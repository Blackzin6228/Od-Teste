#version 150

// Sangue nas paredes e no chão (Sangue), quando algo se aproxima na Vila Calada. Escrito do zero pela mesa Dev.
//
// O sangue é uma CAMADA que absorve luz (não uma tinta por cima): onde é fino, a textura do bloco aparece por
// baixo, manchada de vermelho; onde é grosso, vira quase preto. A espessura varia com coágulos (vermelho escuro
// a quase preto) e a beira forma um anel mais escuro, com uma mancha suave que vaza para fora.
// Parede: faixa de borda rasgada, escura em cima; dela escorrem poucos fios irregulares que ENGROSSAM para baixo
// e terminam numa gota que incha; os fios grossos, quando param, soltam gotas que crescem e caem.
// Chão: poça que cresce a partir de onde a coisa vem, em dedos irregulares.
// Úmido = brilho vermelho escuro (realce da lua/sol e reflexo puxado para vermelho), nunca branco.
// Depois seca (escurece, puxa para marrom), racha e some aos pedaços.
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

const vec3 FRESCO = vec3(0.230, 0.006, 0.014);         // cor própria do sangue grosso iluminado
const vec3 SECO = vec3(0.060, 0.018, 0.012);           // seco: marrom quase preto
const vec3 BRILHO = vec3(0.300, 0.020, 0.022);         // úmido: realce vermelho escuro (nunca branco)

// O que a poça reflete: projeta o raio refletido (bem longe) de volta na tela e lê a cor ali.
vec3 reflexo(vec3 rd, vec3 n) {
    vec3 r = reflect(rd, n);
    vec4 clip = ProjVista * vec4(r * 200.0, 1.0);
    if (clip.w <= 0.0) return vec3(0.0);
    vec2 q = clip.xy / clip.w * 0.5 + 0.5;
    float dentro = smoothstep(0.0, 0.08, q.x) * smoothstep(1.0, 0.92, q.x) * smoothstep(0.0, 0.08, q.y) * smoothstep(1.0, 0.92, q.y);
    return texture(Sampler0, clamp(q, 0.001, 0.999)).rgb * dentro;
}

// Luz que faz o brilho úmido: a lua de noite, o sol de dia (o outro lado).
vec3 luzDir() {
    vec3 l = normalize(LuaDir + vec3(0.0, 1e-4, 0.0));
    return l.y >= 0.0 ? l : -l;
}

// Sangue sobre uma superfície.
//  esp: espessura (0 = só mancha a textura; 1+ = quase preto). corpo: 0..1 quanto da cor própria aparece.
//  molhado: 0..1. n: normal da superfície (com a ondulação do sangue). bump: normal só do relevo do sangue
//  (fio, gota). chao: 1 no chão (tem reflexo).
vec3 tingir(vec3 cena, float esp, float corpo, float molhado, vec3 n, vec3 rd, float chao) {
    float lc = od_lum(cena);
    float luz = clamp(mix(lc, sqrt(lc) * 0.45, 0.5) * 2.0, 0.0, 1.1);
    // 1) a camada absorve: a textura de baixo fica, vermelha e mais escura conforme a espessura
    vec3 r = od_absorve(cena, esp * mix(1.0, 1.35, Seca));
    // 2) cor própria do sangue (onde é grosso o bastante para esconder o bloco), iluminada como a cena
    vec3 propria = mix(FRESCO, SECO, Seca) * (0.05 + luz) * (1.0 - 0.55 * smoothstep(0.6, 1.8, esp));
    r = mix(r, propria, corpo * 0.75);
    // 3) úmido: realce afiado da luz (lua/sol) e reflexo do entorno, tudo puxado para vermelho escuro
    //    luz 1: lua/sol (afiado). luz 2: a luz que vem de perto de quem olha, um pouco de cima (pega o relevo
    //    dos fios, das gotas e dos coágulos). As duas só valem onde a cena tem luz: no escuro, sangue é preto.
    vec3 refl = reflect(rd, n);
    float spec = pow(max(dot(refl, luzDir()), 0.0), 200.0) * 0.9
            + pow(max(dot(refl, normalize(-rd + vec3(0.0, 0.55, 0.0))), 0.0), 36.0) * 0.9;
    float fres = pow(1.0 - clamp(dot(n, -rd), 0.0, 1.0), 4.0);
    float realce = (spec + fres * 0.15) * (0.12 + luz) * molhado;
    vec3 espelho = vec3(0.0);
    if (chao > 0.5 && ComProfundidade > 0.5) espelho = od_lum(reflexo(rd, n)) * vec3(0.18, 0.008, 0.010) * (0.08 + fres);
    r += (BRILHO * realce + espelho * molhado) * smoothstep(0.05, 0.4, esp);
    return od_semBranco(r);
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
        // normal pela profundidade tem ruído de pixel em pixel; o mundo é de blocos: fica só o eixo dominante
        vec3 n0 = od_normal(rel);
        vec3 a0 = abs(n0);
        vec3 n = a0.y >= max(a0.x, a0.z) ? vec3(0.0, sign(n0.y), 0.0)
                : (a0.x >= a0.z ? vec3(sign(n0.x), 0.0, 0.0) : vec3(0.0, 0.0, sign(n0.z)));
        vec3 w = rel + Camera;                 // ponto no mundo (para o desenho não andar com a câmera)
        float perto = 1.0 - smoothstep(Alcance * 1.2, Alcance * 1.6, dist);
        // resultado de cada superfície: cobertura (mancha incluída), espessura, cor própria e relevo
        float cobreP = 0.0, espP = 0.0, corpoP = 0.0;
        float cobreC = 0.0, espC = 0.0, corpoC = 0.0;
        vec3 nP = n, nC = n;

        // tamanho de um pixel na superfície (blocos): some com o detalhe fino de longe em vez de virar listra
        float pix = dist * 2.0 * InvProj[1][1] / max(ScreenSize.y, 1.0);

        // ---- parede
        float parede = 1.0 - smoothstep(0.25, 0.45, abs(n.y));
        if (parede > 0.01 && perto > 0.0) {
            bool emX = abs(n.x) > abs(n.z);
            float u = emX ? w.z : w.x;
            float face = emX ? floor(w.x - n.x * 0.3) : floor(w.z - n.z * 0.3);
            float y = rel.y;
            float pxu = min(pix / max(abs(emX ? rd.x : rd.z), 0.12), 1.0);
            float pxy = pix / max(length(rd.xz), 0.2);
            float fino = 1.0 - smoothstep(0.02, 0.06, pxu);
            float nasce = od_suave(Idade / 1.8);
            vec3 tangente = emX ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
            // faixa: altura muda devagar; a beira de cima é rasgada
            float alto = Chao + 2.6 + 0.9 * od_fbm3(vec2(u * 0.22, face * 3.7));
            float topo = alto + (od_fbm3(vec2(u * 1.9, face + 11.0)) - 0.5) * 0.30
                    + (od_ruido(vec2(u * 6.5, face + 5.0)) - 0.5) * 0.08 * fino;
            float grossa = (0.05 + 0.45 * od_suave(od_fbm3(vec2(u * 0.6, face + 2.0)) * 1.4 - 0.2)) * nasce;
            float base = topo - grossa + (od_ruido(vec2(u * 4.3, face + 8.0)) - 0.5) * 0.07 * fino;
            float aaY = pxy * 0.8 + 0.008;
            float tem = smoothstep(0.44, 0.50, od_fbm3(vec2(u * 0.45, face * 1.3 + 5.0)) + (od_ruido(vec2(u * 5.0, face + 1.0)) - 0.5) * 0.05 * fino);
            float dentro = min(y - base, topo - y);                   // >0 dentro da faixa (blocos)
            float faixa = smoothstep(-aaY, aaY, dentro) * tem;
            // mancha que vaza para fora da faixa (some com a distância da beira)
            float vaza = (1.0 - smoothstep(0.0, 0.10 + 0.08 * od_ruido(vec2(u * 3.0, y * 3.0 + face)), -dentro)) * tem * nasce;
            // coágulos: vermelho escuro a quase preto; mais grosso em cima (onde começou)
            float coag = od_fbm3(vec2(u * 2.6, y * 2.6 + face * 2.0));
            float espF = (0.35 + 1.1 * smoothstep(0.35, 0.75, coag)) * (0.7 + 0.6 * smoothstep(base, topo, y))
                    + 0.6 * (1.0 - smoothstep(0.0, 0.035, dentro));   // anel escuro na beira
            float k = max(faixa, vaza * 0.85);
            float esp = mix(0.10, espF, faixa);
            float corpo = faixa * smoothstep(0.4, 1.2, espF);
            float bx = (coag - 0.5) * 0.8 * faixa * fino;               // relevo (fio, gota, coágulo) para o brilho

            // fios: grossos (poucos, compridos, lentos) e finos (curtos). Engrossam para baixo e acabam numa gota.
            for (int camada = 0; camada < 2; camada++) {
                bool grossoC = camada == 0;
                float colW = grossoC ? 0.83 : 0.29;
                float uu = u + (grossoC ? 0.0 : 37.3);
                float col = floor(uu / colW);
                float sem = od_hash(vec2(col, face * 1.37 + float(camada) * 17.0));
                if (sem > 0.50) continue;
                float h1 = od_hash(vec2(col, face + 7.1 + float(camada) * 3.0));
                float h2 = od_hash(vec2(col, face + 3.3 + float(camada) * 5.0));
                float h3 = od_hash(vec2(col, face + 9.7 + float(camada) * 7.0));
                float centro = (col + 0.15 + 0.7 * h1) * colW;
                float temC = smoothstep(0.44, 0.50, od_fbm3(vec2(centro * 0.45, face * 1.3 + 5.0)));
                if (temC < 0.05) continue;
                float meia = grossoC ? mix(0.026, 0.055, h3) : mix(0.009, 0.020, h3);
                float atraso = (grossoC ? 0.8 : 1.5) + h1 * 4.0;
                float vel = grossoC ? 0.06 + 0.16 * h2 : 0.05 + 0.12 * h2;
                float compr = grossoC ? 0.9 + 2.8 * h3 * h2 : 0.12 + 0.45 * h2;
                float inicio = topo - 0.02;
                float tempoDesce = max(Idade - atraso, 0.0);
                float desce = min(tempoDesce * vel, compr);
                float ponta = max(base - desce, Chao);
                float x = (uu - centro) + (od_ruido(vec2(y * 1.3, col + 0.5)) - 0.5) * (grossoC ? 0.07 : 0.03);
                // engrossa para baixo: fino junto da faixa, cheio perto da ponta (o sangue se acumula)
                float rumo = clamp((base - y) / max(desce, 0.05), 0.0, 1.0);
                float m = meia * mix(0.55, 1.15, od_suave(rumo)) * (0.85 + 0.3 * od_ruido(vec2(y * 4.0, col)));
                float aaU = pxu * 0.75;
                float vert = smoothstep(ponta - aaY, ponta + aaY, y) * (1.0 - smoothstep(inicio - aaY, inicio + aaY, y));
                float corpoF = (1.0 - smoothstep(m - aaU, m + aaU, abs(x))) * vert;
                // gota na ponta: incha enquanto o fio desce
                float rg = meia * (1.4 + 0.8 * od_suave(desce / max(compr, 0.01)));
                float dg = length(vec2(x, (y - ponta - rg * 0.85) * 0.78));
                float gota = (1.0 - smoothstep(rg - aaU, rg + aaU, dg)) * step(0.01, desce);
                // fio grosso parado: uma gota se forma na ponta, cresce e cai (repete)
                float solta = 0.0, dS = 9.0, rS = 1.0;
                if (grossoC && desce >= compr - 1e-3 && ponta > Chao + 0.05) {
                    float fase = fract((tempoDesce - compr / vel) / (2.2 + 1.5 * h3) + h1);
                    float cresce = od_suave(fase / 0.7);
                    float queda = max(fase - 0.7, 0.0);
                    float yG = ponta - rg * 1.2 - queda * queda * 40.0;
                    rS = meia * (0.4 + 0.9 * cresce);
                    dS = length(vec2(x, (y - yG) * 0.75));
                    solta = (1.0 - smoothstep(rS - aaU, rS + aaU, dS)) * step(Chao, yG);
                }
                // mancha em volta do fio (vaza na textura)
                float halo = (1.0 - smoothstep(m, m * 2.6 + aaU, abs(x))) * vert * 0.8;
                float some = 1.0 - smoothstep(meia * 1.2, meia * 3.0, pxu);
                float f = max(max(corpoF, gota), solta) * some * temC * nasce;
                float fh = halo * some * temC * nasce;
                if (f > k * 0.98 || fh > k) {
                    float perfil = clamp(1.0 - abs(x) / max(m, 1e-4), 0.0, 1.0);
                    float noGota = max(gota * (1.0 - dg / rg), solta * (1.0 - dS / rS));
                    float espFio = (grossoC ? 0.9 : 0.55) * (0.4 + 0.9 * sqrt(perfil)) * mix(0.8, 1.5, rumo) + noGota * 1.2;
                    esp = mix(esp, espFio, f);
                    corpo = max(corpo, f * smoothstep(0.3, 1.0, espFio));
                    // relevo: o fio é um cilindro (a normal vira para os lados); a gota é uma bolha
                    bx = (gota > corpoF ? x / max(rg, 1e-4) : x / max(m, 1e-4)) * f;
                    k = max(k, max(f, fh));
                }
            }
            // poça no pé da parede, quando os fios chegam
            float alturaPe = 0.06 + 0.22 * od_fbm3(vec2(u * 1.7, face + 4.0));
            float pe = (1.0 - smoothstep(alturaPe * 0.5, alturaPe + aaY, y - Chao))
                    * od_suave((Idade - 5.0) / 6.0) * smoothstep(0.35, 0.6, od_fbm3(vec2(u * 0.6, face)) + 0.15);
            if (pe * 0.9 > k) {
                esp = 0.6 + 0.8 * (1.0 - smoothstep(0.0, alturaPe, y - Chao)) * coag;
                corpo = pe * 0.8;
                bx = 0.0;
            }
            k = max(k, pe * 0.9);
            cobreP = k * parede;
            espP = esp;
            corpoP = corpo;
            nP = normalize(n + tangente * clamp(bx, -1.0, 1.0) * 0.9 + vec3(0.0, 0.15, 0.0) * abs(bx));
        }

        // ---- chão: poça que cresce a partir da Origem, em dedos, com anel escuro e mancha na beira
        float chao = smoothstep(0.65, 0.85, n.y) * (1.0 - smoothstep(0.6, 1.6, abs(rel.y - Chao)));
        if (chao > 0.01) {
            vec2 d = rel.xz - Origem.xz;
            float r = length(d);
            float frente = Alcance * od_suave(Idade / 9.0);
            float fino = 1.0 - smoothstep(0.03, 0.09, pix);
            float campo = od_fbm(w.xz * 0.42) * 1.15 + 0.30 * od_ruido(w.xz * 2.4) + 0.12 * fino * (od_ruido(w.xz * 9.0) - 0.5) + 0.06;
            float alvo = 1.0 - r / max(frente, 0.01);
            float val = alvo * 0.75 + (campo - 0.78) * 1.5;
            // fios no chão indo para quem olha (a coisa vem atrás dele)
            vec2 paraCam = normalize(-Origem.xz + vec2(1e-4));
            float lado = abs(dot(d, vec2(-paraCam.y, paraCam.x)));
            float lr = lado + (od_ruido(w.xz * 0.9) - 0.5) * 1.4;
            float rio = (0.6 - lr) * step(0.0, dot(d, paraCam)) * (1.0 - smoothstep(frente * 0.9, frente * 1.15, r));
            val = max(val, rio * 0.5);
            float aa = pix * 1.5 / max(abs(rd.y), 0.08) * 0.2 + 0.035;
            float corpoP2 = smoothstep(-aa, aa, val);
            // mancha que vaza: a terra/pedra em volta bebe o sangue (fina, sem cor própria)
            float vaza = smoothstep(-0.16 - 0.06 * od_ruido(w.xz * 3.0), 0.0, val);
            float coag = od_fbm3(w.xz * 1.1 + 9.0);
            float espC0 = 0.6 + 2.2 * smoothstep(0.0, 0.45, val) * (0.25 + 0.75 * coag)
                    + 0.7 * corpoP2 * (1.0 - smoothstep(0.0, 0.07, val));   // anel escuro (sangue que secou na beira)
            cobreC = max(corpoP2, vaza * 0.8) * chao;
            espC = mix(0.12, espC0, corpoP2);
            corpoC = corpoP2 * smoothstep(0.5, 1.6, espC0);
            // superfície: coágulos fazem relevo (gradiente do mesmo ruído) e um tremor leve enquanto está fresca
            vec2 inc = vec2(od_ruido(w.xz * 3.0 + Tempo * 0.25), od_ruido(w.xz * 3.0 + 17.0 - Tempo * 0.2)) - 0.5;
            // (diferença finita de um ruído barato: derivada de tela dentro de "if" não é confiável em toda placa)
            vec2 pc = w.xz * 1.4 + 9.0;
            float r0 = od_ruido(pc);
            vec2 gc = vec2(od_ruido(pc + vec2(0.15, 0.0)) - r0, od_ruido(pc + vec2(0.0, 0.15)) - r0) * 0.6 * fino;
            nC = normalize(vec3(-inc.x * 0.08 - gc.x, 1.0, -inc.y * 0.08 - gc.y));
        }

        bool eChao = cobreC >= cobreP;
        float cobre = max(cobreP, cobreC) * perto * sobra(w.xz + w.y);
        if (cobre > 0.002) {
            float molhado = 1.0 - Seca * 0.92;
            vec3 s = eChao ? tingir(c, espC, corpoC, molhado, nC, rd, 1.0)
                           : tingir(c, espP, corpoP, molhado, nP, rd, 0.0);
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
    float rumo = clamp((1.0 - p.y) / max(desce, 0.01), 0.0, 1.0);    // 0 em cima, 1 na ponta
    float x = fu + (od_ruido(vec2(p.y * 9.0, col)) - 0.5) * 0.4;
    float meiaL = mix(0.16, 0.32, rumo);                           // engrossa para baixo
    float fio = ativa * step(fim, p.y) * (1.0 - smoothstep(meiaL, meiaL + 0.12, abs(x)));
    float rgS = 0.3 + 0.2 * desce / max(maximo, 0.01);
    float gota = ativa * step(0.003, desce) * (1.0 - smoothstep(rgS, rgS + 0.16, length(vec2(x, (p.y - fim) * asp * 40.0))));
    float faixaCima = (1.0 - smoothstep(0.0, 0.02 + 0.03 * od_fbm3(vec2(p.x * 5.0, 3.0)), 1.0 - p.y)) * od_suave(Idade / 2.0);
    faixaCima *= smoothstep(0.1, 0.6, lado + od_fbm3(vec2(p.x * 2.0, 7.0)) * 0.5);
    float baixo = 1.0 - smoothstep(0.0, 0.04, p.y - 0.08 * od_suave(Idade / 8.0) * (0.4 + 1.2 * od_fbm3(vec2(p.x * 6.0, 1.0))) * smoothstep(0.1, 0.8, lado + 0.2));
    float k = max(max(max(fio, gota), baixo), faixaCima) * sobra(p * 8.0);
    float coag = od_fbm3(p * 14.0);
    float esp = (0.5 + 1.0 * coag) * (fio > 0.0 ? mix(0.8, 1.4, rumo) : 1.0) + gota * 0.6;
    vec3 nS = normalize(vec3(clamp(x / 0.24, -1.0, 1.0) * fio * 0.8, 0.2, 1.0));
    // sangue "na lente": não sabemos a luz de onde ele está; fica com um mínimo de luz para ler vermelho, não preto
    vec3 s = tingir(max(c, vec3(0.10, 0.07, 0.07)), esp * 0.8, smoothstep(0.4, 1.2, esp), 1.0 - Seca * 0.92, nS, vec3(0.0, 0.0, -1.0), 0.0);
    c = mix(c, s, k);
    fragColor = vec4(c, 1.0);
}
