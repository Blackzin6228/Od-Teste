#version 150

// Sangue nas paredes e no chão (Sangue), quando algo se aproxima na Vila Calada. Escrito do zero pela mesa Dev.
//
// Nas paredes de verdade (as superfícies em pé que a profundidade mostra) aparece uma faixa molhada de borda rasgada,
// escura em cima, e dela escorrem POUCOS fios irregulares: alguns grossos e lentos, muitos finos e curtos, cada um
// sorteado pela coordenada do mundo. De longe o fio fino some (largura medida em pixels): nada de listra.
// No chão, uma poça cresce a partir de onde a coisa vem, em dedos irregulares, quase preta na beira e vermelho
// profundo no meio. Úmido = só um realce vermelho escuro, raro; nunca branco. Depois seca, racha e some aos pedaços.
// Sem profundidade: o sangue escorre da borda de cima da tela e sobe da borda de baixo.

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InvProj;
uniform mat4 InvView;
uniform mat4 ProjVista;          // projeção × vista (para achar na tela o que a poça reflete)
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

const vec3 BORDA_ESCURA = vec3(0.030, 0.000, 0.002);   // beira do sangue: quase preto
const vec3 FRESCO = vec3(0.260, 0.008, 0.016);         // miolo: vermelho profundo
const vec3 SECO = vec3(0.070, 0.020, 0.013);
const vec3 BRILHO = vec3(0.140, 0.006, 0.008);         // úmido: realce vermelho escuro (nunca branco)

// O que a poça reflete: projeta o raio refletido (bem longe) de volta na tela e lê a cor ali.
// Fora da tela vira escuro. Só no chão.
vec3 reflexo(vec3 rd, vec3 n) {
    vec3 r = reflect(rd, n);
    vec4 clip = ProjVista * vec4(r * 200.0, 1.0);
    if (clip.w <= 0.0) return vec3(0.0);
    vec2 q = clip.xy / clip.w * 0.5 + 0.5;
    float dentro = smoothstep(0.0, 0.08, q.x) * smoothstep(1.0, 0.92, q.x) * smoothstep(0.0, 0.08, q.y) * smoothstep(1.0, 0.92, q.y);
    return texture(Sampler0, clamp(q, 0.001, 0.999)).rgb * dentro;
}

// Cor do sangue sobre uma superfície, iluminado como ela (a luz da cena é estimada pela própria cor).
// miolo: 0 na beira (quase preto), 1 no meio (vermelho profundo). brilho: 0..1, realce úmido escuro.
vec3 tingir(vec3 cena, float miolo, float molhado, float brilho, vec3 n, vec3 rd, float chao) {
    // luz da cena estimada pela cor; achatada um pouco (a camada de sangue esconde a textura de baixo)
    float lc = od_lum(cena);
    float luz = clamp(mix(lc, sqrt(lc) * 0.45, 0.5) * 2.0, 0.0, 1.1);
    vec3 b = mix(BORDA_ESCURA, FRESCO, clamp(miolo, 0.0, 1.0));
    b = mix(b, SECO, Seca);
    // de noite o sangue é quase preto; só fica vermelho onde a luz bate
    vec3 r = b * (0.06 + luz);
    r += BRILHO * brilho * molhado * (0.3 + luz);
    // a poça reflete um pouco do que está em volta, sempre puxado para vermelho escuro
    float fres = pow(1.0 - clamp(dot(n, -rd), 0.0, 1.0), 5.0);
    vec3 espelho = od_lum(reflexo(rd, n)) * vec3(0.30, 0.012, 0.015);
    r = mix(r, max(r, espelho), clamp(0.10 + fres * 0.5, 0.0, 0.6) * molhado * chao * step(0.5, ComProfundidade));
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
        // normal pela profundidade tem ruído de pixel em pixel (virava xadrez); o mundo é de blocos,
        // então fica só o eixo dominante
        vec3 n0 = od_normal(rel);
        vec3 a0 = abs(n0);
        vec3 n = a0.y >= max(a0.x, a0.z) ? vec3(0.0, sign(n0.y), 0.0)
                : (a0.x >= a0.z ? vec3(sign(n0.x), 0.0, 0.0) : vec3(0.0, 0.0, sign(n0.z)));
        vec3 w = rel + Camera;                 // ponto no mundo (para o desenho não andar com a câmera)
        float lumC = od_lum(c);
        float perto = 1.0 - smoothstep(Alcance * 1.2, Alcance * 1.6, dist);
        float cobreP = 0.0, mioloP = 0.0, brilhoP = 0.0;    // parede
        float cobreC = 0.0, mioloC = 0.0;                   // chão
        vec3 nw = n;

        // tamanho de um pixel na superfície (blocos): some com o detalhe fino de longe em vez de virar listra
        float pix = dist * 2.0 * InvProj[1][1] / max(ScreenSize.y, 1.0);

        // ---- parede: uma faixa molhada de borda rasgada, escura em cima, e dela escorrem poucos fios irregulares
        float parede = 1.0 - smoothstep(0.25, 0.45, abs(n.y));
        if (parede > 0.01 && perto > 0.0) {
            bool emX = abs(n.x) > abs(n.z);
            float u = emX ? w.z : w.x;                              // ao longo da parede (mundo)
            float face = emX ? floor(w.x - n.x * 0.3) : floor(w.z - n.z * 0.3);   // 0.3: estável em parede inteira e meio bloco
            float y = rel.y;
            float pxu = min(pix / max(abs(emX ? rd.x : rd.z), 0.12), 1.0);   // pixel ao longo da parede
            float pxy = pix / max(length(rd.xz), 0.2);
            float fino = 1.0 - smoothstep(0.02, 0.06, pxu);          // detalhe fino só de perto
            float nasce = od_suave(Idade / 1.8);
            // faixa: altura muda devagar; a beira de cima é rasgada (duas escalas de ruído, a miúda só de perto)
            float alto = Chao + 2.6 + 0.9 * od_fbm3(vec2(u * 0.22, face * 3.7));
            float topo = alto + (od_fbm3(vec2(u * 1.9, face + 11.0)) - 0.5) * 0.30
                    + (od_ruido(vec2(u * 6.5, face + 5.0)) - 0.5) * 0.08 * fino;
            float grossa = (0.05 + 0.45 * od_suave(od_fbm3(vec2(u * 0.6, face + 2.0)) * 1.4 - 0.2)) * nasce;
            float base = topo - grossa + (od_ruido(vec2(u * 4.3, face + 8.0)) - 0.5) * 0.07 * fino;
            float aaY = pxy * 0.8 + 0.008;
            float tem = smoothstep(0.44, 0.50, od_fbm3(vec2(u * 0.45, face * 1.3 + 5.0)) + (od_ruido(vec2(u * 5.0, face + 1.0)) - 0.5) * 0.05 * fino);   // trechos sem sangue
            float faixa = smoothstep(base - aaY, base + aaY, y) * (1.0 - smoothstep(topo - aaY, topo + aaY, y)) * tem;
            // escura em cima, escorrendo para baixo
            float mioloFaixa = 0.25 + 0.6 * (1.0 - smoothstep(base, topo, y)) * od_fbm3(vec2(u * 3.0, y * 2.0 + face));
            float k = faixa;
            float miolo = mioloFaixa;
            float brilho = 0.0;

            // fios: duas camadas. Grossos (poucos, compridos, lentos) e finos (curtos). Uma coluna irregular por fio,
            // com posição, largura, atraso, velocidade e comprimento sorteados pela coordenada do mundo.
            for (int camada = 0; camada < 2; camada++) {
                bool grossoC = camada == 0;
                float colW = grossoC ? 0.83 : 0.29;
                float uu = u + (grossoC ? 0.0 : 37.3);
                float col = floor(uu / colW);
                float sem = od_hash(vec2(col, face * 1.37 + float(camada) * 17.0));
                if (sem > (grossoC ? 0.50 : 0.50)) continue;          // a maioria das colunas não tem fio
                float h1 = od_hash(vec2(col, face + 7.1 + float(camada) * 3.0));
                float h2 = od_hash(vec2(col, face + 3.3 + float(camada) * 5.0));
                float h3 = od_hash(vec2(col, face + 9.7 + float(camada) * 7.0));
                float centro = (col + 0.15 + 0.7 * h1) * colW;
                // só nasce fio onde a faixa existe naquela coluna
                float temC = smoothstep(0.44, 0.50, od_fbm3(vec2(centro * 0.45, face * 1.3 + 5.0)));
                if (temC < 0.05) continue;
                float meia = grossoC ? mix(0.030, 0.065, h3) : mix(0.010, 0.022, h3);
                float atraso = (grossoC ? 0.8 : 1.5) + h1 * 4.0;
                float vel = grossoC ? 0.06 + 0.16 * h2 : 0.05 + 0.12 * h2;
                float compr = grossoC ? 0.9 + 2.8 * h3 * h2 : 0.12 + 0.45 * h2;
                float inicio = topo - 0.02;
                float desce = min(max(Idade - atraso, 0.0) * vel, compr);
                float ponta = max(base - desce, Chao);
                // o fio entorta devagar (ruído baixo em y), afina perto da ponta e engorda na gota
                float x = (uu - centro) + (od_ruido(vec2(y * 1.3, col + 0.5)) - 0.5) * (grossoC ? 0.07 : 0.03);
                float afina = mix(0.45, 1.0, od_suave((y - ponta) / max(desce * 0.7, 0.05)));
                float m = meia * afina;
                float aaU = pxu * 0.75;
                float corpo = 1.0 - smoothstep(m - aaU, m + aaU, abs(x));
                corpo *= smoothstep(ponta - aaY, ponta + aaY, y) * (1.0 - smoothstep(inicio - aaY, inicio + aaY, y));
                float rg = meia * 1.5;
                float dg = length(vec2(x, (y - ponta - rg * 0.9) * 0.8));
                float gota = (1.0 - smoothstep(rg - aaU, rg + aaU, dg)) * step(0.01, desce);
                // de longe o fio fino some (fica do tamanho de menos de um pixel): nada de virar listra
                float some = 1.0 - smoothstep(meia * 1.2, meia * 3.0, pxu);
                float f = max(corpo, gota) * some * temC * nasce;
                if (f > k) {
                    k = f;
                    float perfil = clamp(1.0 - abs(x) / max(m, 1e-4), 0.0, 1.0);
                    miolo = mix(0.15, 1.0, sqrt(perfil)) * (grossoC ? 1.0 : 0.75);
                    // realce úmido: raro (só alguns fios grossos), fino, deslocado do centro
                    float raro = step(0.75, h2) * float(grossoC);
                    brilho = raro * exp(-pow((x / max(m, 1e-4) - 0.35) / 0.18, 2.0)) * fino;
                }
            }
            // poça no pé da parede, quando os fios chegam (borda ondulada e suave)
            float alturaPe = 0.06 + 0.22 * od_fbm3(vec2(u * 1.7, face + 4.0));
            float pe = (1.0 - smoothstep(alturaPe * 0.5, alturaPe + aaY, y - Chao))
                    * od_suave((Idade - 5.0) / 6.0) * smoothstep(0.35, 0.6, od_fbm3(vec2(u * 0.6, face)) + 0.15);
            if (pe * 0.9 > k) {
                miolo = 0.3 + 0.5 * (1.0 - smoothstep(0.0, alturaPe, y - Chao));
                brilho = 0.0;
            }
            k = max(k, pe * 0.9);
            cobreP = k * parede;
            mioloP = miolo;
            brilhoP = brilho;
        }

        // ---- chão: poça que cresce a partir da Origem, em dedos, com a beira suave e mais escura
        float chao = smoothstep(0.65, 0.85, n.y) * (1.0 - smoothstep(0.6, 1.6, abs(rel.y - Chao)));
        if (chao > 0.01) {
            vec2 d = rel.xz - Origem.xz;
            float r = length(d);
            float frente = Alcance * od_suave(Idade / 9.0);
            float fino = 1.0 - smoothstep(0.03, 0.09, pix);
            float campo = od_fbm(w.xz * 0.42) * 1.15 + 0.30 * od_ruido(w.xz * 2.4) + 0.12 * fino * (od_ruido(w.xz * 9.0) - 0.5) + 0.06;
            float alvo = 1.0 - r / max(frente, 0.01);         // 1 no centro, 0 na frente
            float val = alvo * 0.75 + (campo - 0.78) * 1.5;
            // borda: largura acompanha o pixel (sem serrilhado) e uma faixa suave de ruído
            float aa = pix * 1.5 / max(abs(rd.y), 0.08) * 0.2 + 0.035;
            float k = smoothstep(-aa, aa, val);
            // beira quase preta, miolo vermelho; manchas mais fundas e mais rasas pelo meio
            float miolo = smoothstep(0.0, 0.5, val) * (0.45 + 0.55 * od_fbm3(w.xz * 0.8 + 9.0));
            // fios no chão indo para quem olha (a coisa vem atrás dele)
            vec2 paraCam = normalize(-Origem.xz + vec2(1e-4));
            float lado = abs(dot(d, vec2(-paraCam.y, paraCam.x)));
            float lr = lado + (od_ruido(w.xz * 0.9) - 0.5) * 1.4;
            float rio = (1.0 - smoothstep(0.2, 0.75, lr))
                    * step(0.0, dot(d, paraCam)) * (1.0 - smoothstep(frente * 0.9, frente * 1.15, r));
            if (rio * 0.95 > k) miolo = max(miolo, 0.6 * (1.0 - smoothstep(0.0, 0.6, lr)));
            k = max(k, rio * 0.95);
            cobreC = k * chao;
            mioloC = miolo;
            // superfície da poça: lisa, com um tremor leve enquanto está fresca
            vec2 inc = vec2(od_ruido(w.xz * 3.0 + Tempo * 0.25), od_ruido(w.xz * 3.0 + 17.0 - Tempo * 0.2)) - 0.5;
            nw = normalize(vec3(-inc.x * 0.06, 1.0, -inc.y * 0.06));
        }

        bool eChao = cobreC >= cobreP;
        float cobre = max(cobreP, cobreC) * perto * sobra(w.xz + w.y);
        if (cobre > 0.002) {
            float molhado = 1.0 - Seca * 0.92;
            vec3 s = tingir(c, eChao ? mioloC : mioloP, molhado, eChao ? 0.0 : brilhoP, eChao ? nw : n, rd, eChao ? 1.0 : 0.0);
            // seco: racha e escurece nas emendas
            float racha = 1.0 - smoothstep(0.0, 0.06, abs(od_ruido(w.xz * 4.0 + w.y) - 0.5));
            s *= 1.0 - racha * Seca * 0.55 * (1.0 - smoothstep(0.03, 0.08, pix));
            c = mix(c, s, clamp(cobre * 1.1, 0.0, 1.0));
        }
        fragColor = vec4(c, 1.0);
        return;
    }

    // ---- sem profundidade: escorre da borda de cima (mais nos cantos, nunca no meio da mira), sobe da de baixo
    float asp = ScreenSize.x / max(ScreenSize.y, 1.0);
    vec2 p = vec2(uv.x * asp, uv.y);
    float lado = abs(uv.x - 0.5) * 2.0;                       // 0 no meio, 1 na borda
    float larg = 0.012;
    float col = floor(p.x / larg);
    float fu = fract(p.x / larg) - 0.5;
    float semente = od_hash(vec2(col, 2.0));
    float ativa = step(mix(0.93, 0.45, smoothstep(0.2, 0.9, lado)), semente);
    float atraso = od_hash(vec2(col, 4.0)) * 4.0;
    float maximo = mix(0.08, 0.38, smoothstep(0.3, 1.0, lado)) * (0.5 + 0.5 * od_hash(vec2(col, 8.0)));
    float desce = min(max(Idade - atraso, 0.0) * (0.015 + 0.03 * od_hash(vec2(col, 6.0))), maximo);
    float fim = 1.0 - desce;
    float x = fu + (od_ruido(vec2(p.y * 9.0, col)) - 0.5) * 0.4;
    float fio = ativa * step(fim, p.y) * (1.0 - smoothstep(0.16, 0.3, abs(x)));
    float gota = ativa * step(0.003, desce) * (1.0 - smoothstep(0.3, 0.48, length(vec2(x, (p.y - fim) * asp * 40.0))));
    float faixaCima = (1.0 - smoothstep(0.0, 0.02 + 0.03 * od_fbm3(vec2(p.x * 5.0, 3.0)), 1.0 - p.y)) * od_suave(Idade / 2.0);
    faixaCima *= smoothstep(0.1, 0.6, lado + od_fbm3(vec2(p.x * 2.0, 7.0)) * 0.5);
    float baixo = 1.0 - smoothstep(0.0, 0.04, p.y - 0.08 * od_suave(Idade / 8.0) * (0.4 + 1.2 * od_fbm3(vec2(p.x * 6.0, 1.0))) * smoothstep(0.1, 0.8, lado + 0.2));
    float k = max(max(max(fio, gota), baixo), faixaCima) * sobra(p * 8.0);
    vec3 s = tingir(c, 0.6, 1.0 - Seca * 0.92, 0.0, vec3(0.0, 0.0, 1.0), vec3(0.0, 0.0, -1.0), 0.0);
    c = mix(c, s, k);
    fragColor = vec4(c, 1.0);
}
