#version 150

// Sangue nas paredes e no chão (Sangue), quando algo se aproxima na Vila Calada. Escrito do zero pela mesa Dev.
//
// Fios de sangue nascem nas paredes de verdade (as superfícies em pé que a profundidade mostra), descem devagar
// com uma gota pesada na ponta e chegam ao chão. No chão, uma poça cresce a partir de onde a coisa vem,
// em dedos irregulares, escura e molhada (brilha a lua e a luz das tochas). Depois seca: perde o brilho,
// vira marrom-preto, racha e some aos pedaços. Tudo preso ao mundo (não anda com a câmera).
// Sem profundidade: o sangue escorre da borda de cima da tela e sobe da borda de baixo.

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InvProj;
uniform mat4 InvView;
uniform vec2 ScreenSize;
uniform float Tempo;
uniform float Forca;             // 0..1: some aos pedaços quando cai
uniform float ComProfundidade;
uniform vec3 LuaDir;
uniform vec3 Camera;             // posição da câmera (mod 4096)
uniform float Idade;             // segundos desde que começou
uniform float Seca;              // 0 = fresco, 1 = seco
uniform vec3 Origem;             // de onde a coisa vem, no chão, relativo à câmera
uniform float Chao;              // altura do chão do evento, relativa à câmera
uniform float Alcance;           // até onde a poça chega (blocos)

in vec2 texCoord;
out vec4 fragColor;

#moj_import <odteste:odfx_comum.glsl>

const vec3 FRESCO_FUNDO = vec3(0.060, 0.000, 0.004);
const vec3 FRESCO = vec3(0.300, 0.012, 0.022);
const vec3 SECO = vec3(0.075, 0.022, 0.014);

// Cor do sangue sobre uma superfície, iluminado como ela (a luz da cena é estimada pela própria cor).
vec3 tingir(vec3 cena, float grosso, float molhado, vec3 n, vec3 rd, float brilhoLuz) {
    float luz = clamp(od_lum(cena) * 2.6, 0.0, 1.5);
    vec3 b = mix(FRESCO, FRESCO_FUNDO, clamp(grosso, 0.0, 1.0) * 0.75);
    b = mix(b, SECO, Seca);
    // de noite o sangue é quase preto; só fica vermelho onde a luz bate
    vec3 r = b * (0.05 + luz * 1.0);
    // um pouco da luz quente da cena atravessa a beira fina
    r += cena * vec3(0.22, 0.015, 0.015) * (1.0 - grosso) * (1.0 - Seca);
    // molhado: um reflexo fino da lua e da luz que já bate ali; o resto reflete o céu escuro
    vec3 h = normalize(normalize(LuaDir) - rd);
    float esp = pow(max(dot(n, h), 0.0), 220.0) * 0.55;
    float fres = pow(1.0 - clamp(dot(n, -rd), 0.0, 1.0), 5.0);
    r += vec3(0.55, 0.30, 0.30) * esp * molhado;
    r = mix(r, vec3(0.020, 0.004, 0.006), fres * molhado * 0.5);
    r += vec3(0.8, 0.20, 0.14) * pow(brilhoLuz, 2.0) * molhado * 0.35;
    return r;
}

// Some aos pedaços (não desbota liso): cada mancha tem sua hora de sumir.
float sobra(vec2 w) {
    float n = od_fbm3(w * 1.7);
    return smoothstep(1.0 - Forca, 1.0 - Forca + 0.12, n * 0.9 + 0.1);
}

void main() {
    vec2 uv = texCoord;
    vec3 c = texture(Sampler0, uv).rgb;
    if (Forca < 0.002) {
        fragColor = vec4(c, 1.0);
        return;
    }
    vec3 rd = od_raio(InvProj, InvView, uv);

    if (ComProfundidade > 0.5) {
        vec4 v = od_vista(Sampler1, InvProj, uv);
        if (v.w < 0.5) {
            fragColor = vec4(c, 1.0);
            return;
        }
        vec3 rel = od_rel(InvView, v.xyz);
        float dist = length(rel);
        vec3 n = od_normal(rel);
        vec3 w = rel + Camera;                 // ponto no mundo (para o desenho não andar com a câmera)
        float lumC = od_lum(c);
        float perto = 1.0 - smoothstep(Alcance * 1.2, Alcance * 1.6, dist);
        float cobre = 0.0;
        float grosso = 0.0;
        vec3 nw = n;

        // ---- parede: fios que descem
        float parede = 1.0 - smoothstep(0.25, 0.45, abs(n.y));
        if (parede > 0.01 && perto > 0.0) {
            float u = abs(n.x) > abs(n.z) ? w.z : w.x;           // ao longo da parede
            float faceId = abs(n.x) > abs(n.z) ? floor(w.x - n.x * 0.5) : floor(w.z - n.z * 0.5);
            float largCol = 0.22;
            float col = floor(u / largCol);
            float fu = fract(u / largCol);
            float semente = od_hash(vec2(col, faceId * 1.37));
            float ativa = step(0.55, semente);                  // nem toda coluna tem fio
            float atraso = od_hash(vec2(col, 7.1 + faceId)) * 3.5;
            float vel = 0.35 + 0.5 * od_hash(vec2(col, 3.3 + faceId));
            float topo = Chao + 1.6 + 2.6 * od_hash(vec2(col, 5.9 + faceId));
            float desce = max(Idade - atraso, 0.0) * vel;
            float fim = max(topo - desce, Chao);
            float y = rel.y;
            // o fio balança um pouco e afina para baixo; uma gota mais gorda na ponta
            float x = fu - 0.5 + (od_ruido(vec2(y * 2.3, col)) - 0.5) * 0.35;
            float largura = mix(0.07, 0.17, od_hash(vec2(col, 9.7))) * mix(0.55, 1.0, od_suave((y - fim) / max(topo - fim, 0.01)));
            float dentro = step(fim, y) * step(y, topo + 0.05);
            float fio = (1.0 - smoothstep(largura * 0.6, largura, abs(x))) * dentro;
            float gota = 1.0 - smoothstep(0.16, 0.24, length(vec2(x, (y - fim - 0.08) * 1.6)));
            gota *= step(Chao + 0.05, fim) * step(0.01, desce);
            // mancha borrada onde o fio nasce
            float borrao = od_ruido(vec2(u * 6.0, y * 9.0));
            float nasce = (1.0 - smoothstep(0.2, 1.0, length(vec2(x * 0.35, (y - topo - 0.05) * 3.5)) + (borrao - 0.5) * 0.9))
                    * od_suave(desce * 2.0) * 0.55;
            float k = max(max(fio, gota), nasce * 0.8) * ativa;
            // poça no pé da parede
            float pe = (1.0 - smoothstep(0.0, 0.35 + 0.25 * od_ruido(vec2(u * 3.0, faceId)), y - Chao)) * od_suave((Idade - 3.0) / 4.0);
            k = max(k, pe * 0.9);
            cobre = max(cobre, k * parede);
            grosso = max(grosso, mix(0.3, 1.0, fio) * k);
        }

        // ---- chão: poça que cresce a partir da Origem, em dedos
        float chao = smoothstep(0.65, 0.85, n.y) * (1.0 - smoothstep(0.6, 1.6, abs(rel.y - Chao)));
        if (chao > 0.01) {
            vec2 d = rel.xz - Origem.xz;
            float r = length(d);
            float frente = Alcance * od_suave(Idade / 9.0);
            float campo = od_fbm(w.xz * 0.42) * 1.15 + 0.35 * od_ruido(w.xz * 2.4);
            float alvo = 1.0 - r / max(frente, 0.01);         // 1 no centro, 0 na frente
            float k = smoothstep(0.0, 0.07, alvo * 0.75 + (campo - 0.78) * 1.5);
            // fios no chão indo para quem olha (a coisa vem atrás dele)
            vec2 paraCam = normalize(-Origem.xz + vec2(1e-4));
            float lado = abs(dot(d, vec2(-paraCam.y, paraCam.x)));
            float rio = (1.0 - smoothstep(0.25, 0.7, lado + (od_ruido(w.xz * 0.9) - 0.5) * 1.4))
                    * step(0.0, dot(d, paraCam)) * (1.0 - smoothstep(frente * 0.9, frente * 1.15, r));
            k = max(k, rio * 0.95);
            cobre = max(cobre, k * chao);
            grosso = max(grosso, clamp(alvo * 1.6 + campo - 0.6, 0.0, 1.0) * k);
            // superfície da poça: lisa, com um tremor leve enquanto está fresca
            vec2 inc = vec2(od_ruido(w.xz * 3.0 + Tempo * 0.25), od_ruido(w.xz * 3.0 + 17.0 - Tempo * 0.2)) - 0.5;
            nw = normalize(vec3(-inc.x * 0.06, 1.0, -inc.y * 0.06));
        }

        cobre *= perto * sobra(w.xz + w.y);
        if (cobre > 0.002) {
            float molhado = 1.0 - Seca * 0.92;
            vec3 s = tingir(c, grosso, molhado, nw, rd, smoothstep(0.25, 0.6, lumC));
            // seco: racha e escurece nas emendas
            float racha = 1.0 - smoothstep(0.0, 0.06, abs(od_ruido(w.xz * 4.0 + w.y) - 0.5));
            s *= 1.0 - racha * Seca * 0.55;
            // borda da poça um pouco mais escura (o sangue engrossa na beira)
            float borda = smoothstep(0.0, 0.35, cobre) * (1.0 - smoothstep(0.35, 0.9, cobre));
            s *= 1.0 - borda * 0.5;
            c = mix(c, s, smoothstep(0.0, 0.25, cobre));
        }
        fragColor = vec4(c, 1.0);
        return;
    }

    // ---- sem profundidade: escorre de cima da tela, sobe de baixo
    float asp = ScreenSize.x / max(ScreenSize.y, 1.0);
    vec2 p = vec2(uv.x * asp, uv.y);
    float larg = 0.018;
    float col = floor(p.x / larg);
    float fu = fract(p.x / larg) - 0.5;
    float semente = od_hash(vec2(col, 2.0));
    float atraso = od_hash(vec2(col, 4.0)) * 3.0;
    float desce = max(Idade - atraso, 0.0) * (0.03 + 0.06 * od_hash(vec2(col, 6.0)));
    float fim = 1.0 - desce;
    float x = fu + (od_ruido(vec2(p.y * 9.0, col)) - 0.5) * 0.4;
    float fio = step(0.55, semente) * step(fim, p.y) * (1.0 - smoothstep(0.18, 0.32, abs(x)));
    float gota = step(0.55, semente) * (1.0 - smoothstep(0.32, 0.5, length(vec2(x, (p.y - fim) * asp * 30.0))));
    float baixo = 1.0 - smoothstep(0.0, 0.05, p.y - 0.12 * od_suave(Idade / 8.0) * (0.6 + 0.8 * od_fbm3(vec2(p.x * 6.0, 1.0))));
    float k = max(max(fio, gota), baixo) * sobra(p * 8.0);
    vec3 s = tingir(c, 0.7, 1.0 - Seca * 0.92, vec3(0.0, 0.0, 1.0), vec3(0.0, 0.0, -1.0), 0.0);
    c = mix(c, s, k);
    fragColor = vec4(c, 1.0);
}
