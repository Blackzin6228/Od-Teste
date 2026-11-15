#version 150

// Lua e névoa carmesim (Sangue), à noite na Vila Calada. Escrito do zero pela mesa Dev.
//
// O céu afunda num vermelho quase preto com duas camadas de nuvem (uma alta e lenta, outra baixa e torcida que
// passa na frente da lua). A lua vira ferida: superfície manchada, borda escura, coroa de raios que respira.
// A névoa de sangue tem camadas: um véu longe, uma névoa rasteira que rola em fiapos colada no chão e uma
// cinza escura que flutua perto do olho. Cor nunca lisa: vinho, quase preto e um vermelho vivo do lado da lua.
// Fora do vermelho, a cor do mundo é lavada. Sem profundidade (shader pack): o mesmo tom, névoa pela direção.

uniform sampler2D Sampler0;      // cor da cena
uniform sampler2D Sampler1;      // profundidade da cena
uniform mat4 InvProj;
uniform mat4 InvView;
uniform vec2 ScreenSize;
uniform float Tempo;             // segundos
uniform float Forca;             // 0..1 (entrada, noite, efeitos_fortes)
uniform float ComProfundidade;   // 1 = profundidade boa; 0 = versão sem profundidade
uniform vec3 LuaDir;             // direção da lua, eixos do mundo
uniform vec3 Camera;             // posição da câmera (mod 4096), para a névoa não andar junto

in vec2 texCoord;
out vec4 fragColor;

#moj_import <odteste:odfx_comum.glsl>

// Paleta do Sangue (docs/decisoes/2026-11-08-cores-dos-elementos.md): dois tons e o preto.
const vec3 SANGUE_ESCURO = vec3(0.180, 0.004, 0.012);
const vec3 SANGUE = vec3(0.478, 0.039, 0.063);   // #7A0A10
const vec3 CEU_ALTO = vec3(0.008, 0.000, 0.002);
const vec3 HORIZONTE = vec3(0.120, 0.014, 0.018);

// Lava a cor: o que não é vermelho vira cinza escuro; o vermelho fica.
vec3 lavar(vec3 c, float k) {
    float l = od_lum(c);
    float vermelho = smoothstep(0.25, 0.7, (c.r - max(c.g, c.b)) / max(c.r, 0.015));
    vec3 cinza = vec3(l) * vec3(1.0, 0.93, 0.92);
    vec3 r = mix(cinza, c, vermelho * 0.85);
    r *= mix(vec3(1.0), vec3(1.10, 0.80, 0.80), smoothstep(0.25, 0.0, l));
    return mix(c, r, k);
}

// Base em volta da lua (para manchar a superfície e girar a coroa).
void baseLua(vec3 l, out vec3 bx, out vec3 by) {
    bx = normalize(cross(l, abs(l.y) < 0.95 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0)));
    by = cross(bx, l);
}

vec3 ceu(vec3 c, vec3 rd, float k) {
    float y = rd.y;
    float lum = od_lum(c);
    vec3 l = normalize(LuaDir);
    vec3 bx, by;
    baseLua(l, bx, by);
    vec2 ql = vec2(dot(rd, bx), dot(rd, by));        // posição em volta da lua (radianos, mais ou menos)
    float cosl = max(dot(rd, l), 0.0);
    float ang = acos(clamp(cosl, -1.0, 1.0));

    // gradiente: horizonte vinho, alto quase preto, com uma faixa mais quente torcida no horizonte
    vec3 base = mix(HORIZONTE, CEU_ALTO, od_suave(y * 2.2));
    vec2 q = rd.xz / max(y + 0.25, 0.08);
    // nuvem alta: fina, lenta
    float alta = smoothstep(0.50, 0.85, od_fbm3(q * 0.7 + vec2(Tempo * 0.006, 0.0)));
    // nuvem baixa: torcida, rola mais rápido, é ela que corta a lua
    float baixa = smoothstep(0.38, 0.88, od_torcido(q * 1.4 + 3.0, Tempo * 0.035)) * smoothstep(0.06, 0.32, y);
    alta *= smoothstep(0.02, 0.25, y);
    // halo: três raios + coroa de raios irregulares que respira
    float raios = od_fbm3(vec2(atan(ql.y, ql.x) * 5.0 / OD_PI + Tempo * 0.02, ang * 4.0 - Tempo * 0.05));
    float coroa = exp(-ang * 9.0) * (0.35 + 0.9 * raios) * (0.85 + 0.15 * sin(Tempo * 0.7));
    float halo = pow(cosl, 120.0) * 0.6 + coroa * 0.45 + pow(cosl, 4.0) * 0.06;
    vec3 r = base + mix(SANGUE, vec3(0.62, 0.08, 0.05), raios * 0.5) * halo * (1.0 - baixa * 0.6);
    // borda das nuvens baixas pega a luz da lua (vermelho sujo), o miolo fica escuro
    float aro = baixa * (1.0 - baixa) * 4.0;
    r += vec3(0.30, 0.025, 0.02) * aro * (exp(-ang * 3.0) * 0.8 + 0.08);
    r = mix(r, SANGUE_ESCURO * (0.25 + halo * 1.6), baixa * 0.7);
    r = mix(r, r * vec3(0.7, 0.55, 0.6) + SANGUE_ESCURO * 0.15, alta * 0.5);

    // a própria lua: o brilho que já existe vira ferida manchada, com borda mais escura e núcleo vivo
    float lua = smoothstep(0.35, 0.9, lum) * smoothstep(0.25, 0.0, ang);
    float mancha = od_fbm3(ql * 60.0 + 7.0);
    float veio = 1.0 - smoothstep(0.0, 0.06, abs(od_fbm3(ql * 35.0 - 3.0) - 0.5));
    vec3 ferida = mix(vec3(0.30, 0.020, 0.018), vec3(0.88, 0.20, 0.10), smoothstep(0.80, 1.0, lum) * smoothstep(0.25, 0.75, mancha));
    ferida *= 1.0 - veio * 0.55;                        // fissuras escuras na superfície
    ferida *= 0.86 + 0.14 * sin(Tempo * 0.9);
    r += ferida * lua * (1.0 - baixa * 0.8);
    // estrelas viram brasa fraca e piscam
    float estrela = smoothstep(0.05, 0.2, lum) * (1.0 - lua) * smoothstep(0.1, 0.0, halo);
    r += vec3(0.26, 0.02, 0.015) * estrela * (0.35 + 0.35 * sin(Tempo * 3.0 + lum * 50.0)) * (1.0 - baixa);
    return mix(c, r, k);
}

// Cinza escura que flutua perto do olho: poucas partículas, em três profundidades (parallax pela direção).
float fuligem(vec3 rd) {
    float s = 0.0;
    vec2 a = vec2(atan(rd.x, rd.z), asin(clamp(rd.y, -1.0, 1.0)));
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float esc = 9.0 + fi * 7.0;
        vec2 p = a * esc + vec2(Tempo * (0.05 + fi * 0.03), Tempo * (0.12 - fi * 0.02)) + Camera.xz * (0.04 * (fi + 1.0));
        vec2 cel = floor(p);
        vec2 f = fract(p) - 0.5;
        float h = od_hash(cel + fi * 13.0);
        if (h > 0.10) continue;                         // só uma célula em dez tem cinza
        vec2 o = vec2(od_hash(cel + 3.1), od_hash(cel + 9.7)) - 0.5;
        o += 0.15 * vec2(sin(Tempo * 0.8 + h * 60.0), cos(Tempo * 0.6 + h * 40.0));
        float tam = 0.06 + 0.08 * od_hash(cel + 5.5);
        s += (1.0 - smoothstep(0.0, tam, length(f - o * 0.7))) * (0.5 + 0.5 * od_hash(cel + 2.2));
    }
    return clamp(s, 0.0, 1.0);
}

// Grão de filme e cantos mais escuros: tira a cara de filtro limpo.
vec3 acabamento(vec3 c, vec2 uv, float k) {
    float g = od_hash(uv * ScreenSize + fract(Tempo * 7.0) * 113.0) - 0.5;
    c += g * 0.035 * k * (0.3 + od_lum(c));
    vec2 d = uv - 0.5;
    c *= 1.0 - k * 0.30 * od_suave(dot(d, d) * 2.2);
    return max(od_pontilhar(c, uv * ScreenSize), 0.0);
}

// Cor da névoa: nunca lisa. Vinho escuro longe, quase preto nos fiapos grossos, vermelho do lado da lua.
vec3 corNevoa(float longe, float deriva, float lado) {
    vec3 cor = mix(HORIZONTE * 0.45, HORIZONTE * 1.1, longe);
    cor = mix(cor, SANGUE_ESCURO * 0.35, smoothstep(0.55, 0.85, deriva) * 0.7);   // miolo grosso escurece
    cor += SANGUE * 0.45 * lado * (0.5 + deriva);                                 // luz da lua atravessa
    return cor;
}

void main() {
    vec2 uv = texCoord;
    vec3 c = texture(Sampler0, uv).rgb;
    float k = clamp(Forca, 0.0, 1.0);
    if (k < 0.002) {
        fragColor = vec4(c, 1.0);
        return;
    }
    vec3 rd = od_raio(InvProj, InvView, uv);
    float lado = pow(max(dot(rd, normalize(LuaDir)), 0.0), 5.0);
    float poeira = fuligem(rd) * k;

    if (ComProfundidade > 0.5) {
        vec4 v = od_vista(Sampler1, InvProj, uv);
        if (v.w < 0.5) {
            vec3 r = ceu(c, rd, k);
            r = mix(r, vec3(0.012, 0.0, 0.002), poeira * 0.7);
            fragColor = vec4(acabamento(r, uv, k), 1.0);
            return;
        }
        vec3 rel = od_rel(InvView, v.xyz);
        float dist = length(rel);
        vec3 r = lavar(c, k * 0.85);
        // névoa em camadas. 1) véu longe (pela distância)
        float longe = 1.0 - exp(-dist * 0.036);
        // 2) rasteira: fiapos torcidos que rolam no chão, lidos no ponto do mundo e meio caminho até ele
        float altura = rel.y + 1.62;
        vec2 w = rel.xz + Camera.xz;
        vec2 meio = rel.xz * 0.5 + Camera.xz;
        float deriva = od_torcido(w * 0.07, Tempo * 0.05);
        float fiapo = od_fbm3(meio * 0.18 + vec2(Tempo * 0.04, -Tempo * 0.025));
        float rasteira = exp(-max(altura, 0.0) / mix(0.9, 2.4, fiapo)) * (0.15 + 1.1 * smoothstep(0.30, 0.78, deriva));
        float perto = smoothstep(0.8, 7.0, dist);
        float n = clamp(longe * (0.55 + 0.4 * fiapo), 0.0, 0.94);
        r = mix(r, corNevoa(longe, deriva, lado), n * k);
        // os fiapos rasteiros pegam um pouco de luz (vermelho sujo), com o miolo mais escuro
        float fr = clamp(rasteira * perto * 0.85, 0.0, 0.85);
        vec3 corFiapo = mix(SANGUE * 0.32, SANGUE_ESCURO * 0.5, smoothstep(0.6, 0.9, deriva)) + SANGUE * 0.25 * lado;
        r = mix(r, corFiapo, fr * k);
        r = mix(r, vec3(0.012, 0.0, 0.002), poeira * 0.7);
        fragColor = vec4(acabamento(r, uv, k), 1.0);
        return;
    }

    // ---- sem profundidade: cor lavada em tudo, céu pela direção (só onde já é escuro), névoa pela altura do olhar
    vec3 r = lavar(c, k * 0.85);
    float lum = od_lum(c);
    float acima = smoothstep(0.02, 0.25, rd.y) * clamp(smoothstep(0.12, 0.03, lum) + smoothstep(0.6, 0.9, lum), 0.0, 1.0);
    r = mix(r, ceu(c, rd, 1.0), acima * k * 0.8);
    // brilho em volta da lua (halo/bloom do pack, meio-claro, que o teste acima não pega) vira vermelho
    float pertoLua = smoothstep(0.90, 0.995, dot(rd, normalize(LuaDir))) * smoothstep(0.03, 0.2, lum);
    r = mix(r, vec3(lum) * vec3(1.15, 0.10, 0.08), pertoLua * k);
    float horizonte = exp(-abs(rd.y) * 6.0);
    float deriva = od_torcido(vec2(atan(rd.x, rd.z) * 3.0 + Camera.x * 0.01, rd.y * 4.0), Tempo * 0.05);
    r = mix(r, corNevoa(0.8, deriva, lado), horizonte * (0.45 + 0.3 * deriva) * k);
    r = mix(r, vec3(0.012, 0.0, 0.002), poeira * 0.7);
    fragColor = vec4(acabamento(r, uv, k), 1.0);
}
