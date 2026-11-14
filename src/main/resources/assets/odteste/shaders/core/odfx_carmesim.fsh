#version 150

// Lua e névoa carmesim (Sangue), à noite na Vila Calada. Escrito do zero pela mesa Dev.
//
// O céu afunda num vermelho quase preto, a lua fica cor de ferida com um halo sujo, uma névoa de sangue
// rasteja colada no chão e engole o fundo da rua. Fora do vermelho, a cor do mundo é lavada (quase cinza):
// o vermelho é a única cor viva que sobra. Com profundidade: céu, distância e altura de verdade.
// Sem profundidade (shader pack que não deixa ler): o mesmo tom, com a névoa pela altura do olhar.

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
const vec3 CEU_ALTO = vec3(0.010, 0.000, 0.002);
const vec3 HORIZONTE = vec3(0.120, 0.016, 0.018);

// Lava a cor: o que não é vermelho vira cinza escuro; o vermelho fica.
vec3 lavar(vec3 c, float k) {
    float l = od_lum(c);
    // pela saturação do vermelho (não pelo brilho): sangue escuro continua vermelho
    float vermelho = smoothstep(0.25, 0.7, (c.r - max(c.g, c.b)) / max(c.r, 0.015));
    vec3 cinza = vec3(l) * vec3(1.0, 0.93, 0.92);
    vec3 r = mix(cinza, c, vermelho * 0.85);
    // as sombras puxam para o vinho, as luzes ficam frias e sujas
    r *= mix(vec3(1.0), vec3(1.10, 0.80, 0.80), smoothstep(0.25, 0.0, l));
    return mix(c, r, k);
}

vec3 ceu(vec3 c, vec3 rd, float k) {
    float y = rd.y;
    float lum = od_lum(c);
    // gradiente: horizonte vinho, alto quase preto
    vec3 base = mix(HORIZONTE, CEU_ALTO, od_suave(y * 2.2));
    // nuvens lentas, vermelho sujo, que passam na frente da lua
    vec2 q = rd.xz / max(y + 0.25, 0.08);
    float nuvem = od_fbm3(q * 1.3 + vec2(Tempo * 0.012, Tempo * 0.004));
    nuvem = smoothstep(0.45, 0.85, nuvem);
    vec3 l = normalize(LuaDir);
    float cosl = max(dot(rd, l), 0.0);
    float halo = pow(cosl, 90.0) * 0.55 + pow(cosl, 14.0) * 0.18 + pow(cosl, 3.0) * 0.05;
    vec3 r = base + SANGUE * halo * (1.0 - nuvem * 0.5);
    // a própria lua (e estrelas): o brilho que já existe vira cor de ferida, coberto pelas nuvens
    float lua = smoothstep(0.35, 0.9, lum);
    // lua de cobre podre: escura na borda, um núcleo mais vivo que pulsa devagar
    vec3 ferida = mix(vec3(0.42, 0.035, 0.03), vec3(0.85, 0.22, 0.12), smoothstep(0.80, 1.0, lum));
    ferida *= 0.85 + 0.15 * sin(Tempo * 0.9);
    r += ferida * lua * (1.0 - nuvem * 0.75);
    r += vec3(0.25, 0.02, 0.02) * smoothstep(0.05, 0.2, lum) * (1.0 - lua) * 0.5; // estrelas viram brasa
    r = mix(r, SANGUE_ESCURO * (0.6 + halo * 2.0), nuvem * 0.55);
    return mix(c, r, k);
}

// Grão de filme e cantos mais escuros: tira a cara de filtro limpo.
vec3 acabamento(vec3 c, vec2 uv, float k) {
    float g = od_hash(uv * ScreenSize + fract(Tempo * 7.0) * 113.0) - 0.5;
    c += g * 0.035 * k * (0.3 + od_lum(c));
    vec2 d = uv - 0.5;
    c *= 1.0 - k * 0.30 * od_suave(dot(d, d) * 2.2);
    return max(c, 0.0);
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

    if (ComProfundidade > 0.5) {
        vec4 v = od_vista(Sampler1, InvProj, uv);
        if (v.w < 0.5) {
            fragColor = vec4(acabamento(ceu(c, rd, k), uv, k), 1.0);
            return;
        }
        vec3 rel = od_rel(InvView, v.xyz);
        float dist = length(rel);
        vec3 r = lavar(c, k * 0.85);
        // névoa de sangue: pela distância, mais grossa colada no chão (altura em relação aos pés)
        float altura = rel.y + 1.62;
        vec2 w = (rel.xz + Camera.xz);
        float deriva = od_fbm3(w * 0.06 + vec2(Tempo * 0.03, -Tempo * 0.018));
        float rasteira = exp(-max(altura, 0.0) / 1.6) * (0.55 + 0.6 * deriva);
        float longe = 1.0 - exp(-dist * 0.036);
        float perto = smoothstep(4.0, 16.0, dist);
        float n = clamp(longe * 0.78 + rasteira * perto * 0.55, 0.0, 0.93);
        // a névoa brilha um pouco do lado da lua
        float lado = pow(max(dot(rd, normalize(LuaDir)), 0.0), 6.0);
        vec3 cor = mix(HORIZONTE * mix(0.55, 1.0, longe), SANGUE * 0.55, lado * 0.5 + deriva * 0.12);
        r = mix(r, cor, n * k);
        fragColor = vec4(acabamento(r, uv, k), 1.0);
        return;
    }

    // ---- sem profundidade: cor lavada em tudo, céu pela direção (só onde já é escuro), névoa pela altura do olhar
    vec3 r = lavar(c, k * 0.85);
    float lum = od_lum(c);
    float acima = smoothstep(0.02, 0.25, rd.y) * clamp(smoothstep(0.12, 0.03, lum) + smoothstep(0.6, 0.9, lum), 0.0, 1.0);
    r = mix(r, ceu(c, rd, 1.0), acima * k * 0.8);
    float horizonte = exp(-abs(rd.y) * 6.0);
    float deriva = od_fbm3(vec2(atan(rd.x, rd.z) * 3.0 + Tempo * 0.02, rd.y * 4.0));
    r = mix(r, HORIZONTE * (0.7 + 0.5 * deriva), horizonte * 0.6 * k);
    fragColor = vec4(acabamento(r, uv, k), 1.0);
}
