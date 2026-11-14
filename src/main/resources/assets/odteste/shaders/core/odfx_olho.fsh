#version 150

// Olho no céu (Conhecimento). Escrito do zero pela mesa Dev.
//
// Uma fenda dourada se abre alto no céu. Dentro, um olho pálido e gigante: íris dourada com anéis finos
// que parecem linhas de escrita girando devagar, pupila que dilata e contrai, pálpebras magras de carne
// acinzentada, rachaduras de luz dourada saindo da fenda. Só o céu recebe o olho (profundidade 1);
// o terreno escurece e esfria, e onde ele olha cai uma luz dourada fraca.

uniform sampler2D Sampler0;   // cor da cena
uniform sampler2D Sampler1;   // profundidade da cena
uniform mat4 InvProj;
uniform mat4 InvView;         // vista -> eixos do mundo, relativo à câmera
uniform vec2 ScreenSize;
uniform float Tempo;          // segundos desde que o efeito começou
uniform float Fenda;          // 0..1: o rasgo no céu
uniform float Abertura;       // 0..1: as pálpebras
uniform vec3 OlhoDir;         // direção do centro do olho, eixos do mundo
uniform vec3 Olhar;           // para onde a íris aponta, eixos do mundo
uniform vec3 Alvo;            // ponto do chão onde ele olha, relativo à câmera

in vec2 texCoord;
out vec4 fragColor;

#moj_import <odteste:odfx_comum.glsl>

// Paleta do Conhecimento (docs/decisoes/2026-11-08-cores-dos-elementos.md).
const vec3 OURO_0 = vec3(0.239, 0.180, 0.020);  // #3D2E05
const vec3 OURO_1 = vec3(0.541, 0.416, 0.071);  // #8A6A12
const vec3 OURO_2 = vec3(0.831, 0.659, 0.165);  // #D4A82A
const vec3 OURO_3 = vec3(0.961, 0.863, 0.478);  // #F5DC7A

// Tamanho no plano tangente do céu (tangente do ângulo).
const float LARGURA = 0.34;    // meia largura da fenda
const float ALTURA = 0.125;    // meia altura com o olho todo aberto
const float BOLA = 0.27;       // raio do globo
const float IRIS = 0.40;       // raio da íris (radianos sobre o globo)
const float DISTANCIA = 300.0; // o olho fica a esta distância (blocos), para o feixe

// Ruído com interpolação linear: vira linha quebrada (rachadura), não curva mole.
float quebrado(float x, float semente) {
    float i = floor(x);
    return mix(od_hash(vec2(i, semente)), od_hash(vec2(i + 1.0, semente)), fract(x)) - 0.5;
}

float angDif(float a, float b) {
    float d = mod(a - b + OD_PI, 2.0 * OD_PI) - OD_PI;
    return d;
}

// Meia altura da abertura em x (amêndoa, pálpebra de cima mais pesada).
float meiaAltura(float x, float abre, bool cima) {
    float u = clamp(1.0 - (x * x) / (LARGURA * LARGURA), 0.0, 1.0);
    float h = ALTURA * abre * pow(u, 0.75);
    return cima ? h : h * 0.72;
}

// Rachaduras de luz saindo da fenda: linhas quebradas, cada uma com um galho, afinando para fora.
float rachaduras(vec2 q, float cresce, out float aura) {
    vec2 p = q / vec2(LARGURA, LARGURA * 0.62);
    float r = length(p);
    float a = atan(p.y, p.x);
    float nucleo = 0.0;
    aura = 0.0;
    for (int i = 0; i < 13; i++) {
        float fi = float(i);
        float base = (fi + 0.35 * od_hash(vec2(fi, 3.0))) * (2.0 * OD_PI / 13.0);
        float comp = (0.35 + 0.9 * od_hash(vec2(fi, 8.0))) * cresce;
        float ponta = 0.85 + comp;
        if (r > ponta + 0.05) continue;
        float caminho = base + quebrado(r * 5.0, fi * 3.1) * 0.28 + quebrado(r * 17.0, fi * 5.7) * 0.06;
        float d = abs(angDif(a, caminho)) * r;
        float fim = smoothstep(ponta, ponta - 0.25, r);
        float larg = mix(0.010, 0.0015, clamp((r - 0.8) / max(comp, 0.01), 0.0, 1.0));
        nucleo = max(nucleo, od_linha(d, larg) * fim);
        aura += exp(-d * 70.0) * fim * 0.5;
        // Galho: sai no meio do caminho e abre para um lado.
        float rb = 0.85 + comp * (0.3 + 0.3 * od_hash(vec2(fi, 13.0)));
        if (r > rb) {
            float lado = od_hash(vec2(fi, 21.0)) < 0.5 ? -1.0 : 1.0;
            float cg = caminho + lado * (r - rb) * 0.9 / max(r, 0.3) + quebrado(r * 23.0, fi * 9.1) * 0.05;
            float dg = abs(angDif(a, cg)) * r;
            float fimg = smoothstep(rb + comp * 0.45, rb, r) * fim;
            nucleo = max(nucleo, od_linha(dg, 0.0018) * fimg);
            aura += exp(-dg * 90.0) * fimg * 0.3;
        }
    }
    return nucleo;
}

// Uma faixa de "escrita": traços curtos sobre uma linha de base, com espaços entre palavras.
float escrita(float u, float v) {
    float id = floor(u);
    float f = fract(u);
    float palavra = step(0.2, od_hash(vec2(floor(u / 6.0), 7.3)));
    float traco = 0.0;
    for (int k = 0; k < 2; k++) {
        float fk = float(k);
        float x = (fk + 0.5) / 2.0 + (od_hash(vec2(id, fk)) - 0.5) * 0.25;
        float v0 = 0.12 + od_hash(vec2(id, fk + 11.0)) * 0.35;
        float v1 = 0.55 + od_hash(vec2(id, fk + 23.0)) * 0.35;
        float dentro = smoothstep(v0 - 0.05, v0, v) * smoothstep(v1 + 0.05, v1, v);
        float inclina = (od_hash(vec2(id, fk + 31.0)) - 0.5) * 0.3 * (v - 0.5);
        traco = max(traco, od_linha(f - x - inclina, 0.045) * dentro * step(0.3, od_hash(vec2(id, fk + 5.0))));
    }
    float base = od_linha(v - 0.55, 0.06) * step(0.45, od_hash(vec2(id, 41.0)));
    float ponto = od_linha(length(vec2((f - 0.5) * 0.6, v - 0.12)), 0.05) * step(0.85, od_hash(vec2(id, 51.0)));
    return max(max(traco, base), ponto) * palavra;
}

vec3 globo(vec2 q, vec3 g, float dy, out float emissao) {
    float r2 = dot(q, q);
    float z = sqrt(max(BOLA * BOLA - r2, 0.0));
    vec3 n = normalize(vec3(q, z));

    float ang = acos(clamp(dot(n, g), -1.0, 1.0));
    vec3 t1 = normalize(cross(vec3(0.0, 1.0, 0.0), g));
    vec3 t2 = cross(g, t1);
    float phi = atan(dot(n, t2), dot(n, t1));

    // Pupila: respira devagar e, quando acha quem olha, contrai de uma vez.
    float dil = 0.55 + 0.25 * sin(Tempo * 0.7 + sin(Tempo * 0.27) * 2.5);
    dil -= 0.45 * exp(-max(Tempo - 3.6, 0.0) * 1.2) * step(3.6, Tempo);
    float pupila = mix(0.08, 0.24, clamp(dil, 0.0, 1.0));

    // Esclera: pálida, amarelada de velha, úmida; veias finas cinza-ocre vindo das bordas.
    vec3 cor = vec3(0.80, 0.77, 0.68);
    cor = mix(cor, vec3(0.62, 0.58, 0.50), od_fbm(vec2(phi * 3.0, ang * 4.0)) * 0.6);
    float veias = 0.0;
    for (int k = 0; k < 2; k++) {
        float fk = float(k);
        float v = 1.0 - abs(2.0 * od_ruido(vec2(phi * (5.0 + fk * 6.0) + fk * 9.0, ang * (6.0 + fk * 8.0))) - 1.0);
        veias = max(veias, pow(v, 26.0 + fk * 14.0));
    }
    veias *= od_suave((ang - IRIS * 1.15) * 2.0);
    cor = mix(cor, vec3(0.42, 0.30, 0.26), veias * 0.75);
    // Bola: escurece para as beiradas e para os cantos do olho.
    cor *= mix(0.35, 1.0, pow(n.z, 0.8));
    emissao = 0.0;

    if (ang < IRIS * 1.1) {
        float s = clamp((ang - pupila) / (IRIS - pupila), 0.0, 1.0);
        float fibra = od_ruido(vec2(phi * 30.0, s * 2.0)) * 0.55 + od_ruido(vec2(phi * 80.0, s * 6.0)) * 0.45;
        float cripta = smoothstep(0.62, 0.8, od_ruido(vec2(phi * 9.0, s * 5.0 + 3.0))) * smoothstep(0.25, 0.4, s);
        vec3 iris = mix(OURO_3, OURO_2, od_suave(s * 1.8));
        iris = mix(iris, OURO_1, od_suave((s - 0.5) * 2.2));
        iris *= 0.55 + 0.75 * fibra;
        iris = mix(iris, OURO_0 * 0.5, cripta * 0.7);
        // Colarete: anel claro perto da pupila.
        iris += OURO_3 * exp(-abs(s - 0.18) * 30.0) * 0.5;
        // Borda escura da íris (limbo).
        iris = mix(iris, OURO_0 * 0.35, od_suave((s - 0.82) * 6.0));
        // Anéis de escrita, finos, girando devagar em sentidos alternados.
        float aneis = 0.0;
        for (int k = 0; k < 3; k++) {
            float fk = float(k);
            float c0 = 0.33 + fk * 0.2;
            float larg = 0.075;
            float v = (s - c0) / larg + 0.5;
            if (v > 0.0 && v < 1.0) {
                float giro = Tempo * (0.035 + fk * 0.015) * (mod(fk, 2.0) < 0.5 ? 1.0 : -1.0);
                float n0 = 54.0 + fk * 30.0;
                aneis = max(aneis, escrita((phi / (2.0 * OD_PI) + giro) * n0, v));
            }
        }
        iris = mix(iris, vec3(1.0, 0.96, 0.82), aneis * 0.9);
        float dentro = 1.0 - smoothstep(IRIS * 0.97, IRIS * 1.06, ang);
        cor = mix(cor, iris, dentro);
        emissao = dentro * (0.15 + aneis * 0.9) * (1.0 - s * 0.6);
        // Pupila: buraco preto, com um halo branco-dourado fino.
        float halo = exp(-max(ang - pupila, 0.0) * 55.0);
        cor += vec3(1.0, 0.92, 0.65) * halo * 0.7 * dentro;
        cor = mix(cor, vec3(0.01, 0.008, 0.0), 1.0 - smoothstep(pupila * 0.94, pupila * 1.03, ang));
    }
    // Brilho molhado: uma janela pequena e um reflexo largo bem fraco.
    vec3 refl = reflect(vec3(0.0, 0.0, -1.0), n);
    float spec = pow(max(dot(refl, normalize(vec3(-0.3, 0.5, 0.8))), 0.0), 260.0);
    cor += vec3(1.0, 0.97, 0.9) * spec * 1.4;
    cor += vec3(0.6, 0.55, 0.45) * pow(max(dot(refl, normalize(vec3(0.4, 0.7, 0.6))), 0.0), 12.0) * 0.06;

    // Sombra das pálpebras: forte embaixo da de cima.
    float sombraCima = q.y > 0.0 ? smoothstep(0.0, 0.05, -dy) : smoothstep(0.0, 0.02, -dy);
    cor *= mix(0.15, 1.0, sombraCima);
    emissao *= mix(0.3, 1.0, sombraCima);
    return cor;
}

// Distância do raio (da câmera, direção rd, até tMax) ao segmento a-b.
float distRaioSegmento(vec3 rd, float tMax, vec3 a, vec3 b, out float s) {
    vec3 u = b - a;
    float uu = dot(u, u);
    float ud = dot(u, rd);
    float au = dot(a, u);
    float ad = dot(a, rd);
    float den = uu - ud * ud;
    s = den > 1e-4 ? clamp((ud * ad - au) / den, 0.0, 1.0) : 0.0;
    vec3 p = a + u * s;
    float t = clamp(dot(p, rd), 0.0, tMax);
    return length(p - rd * t);
}

// Vinheta fria nas bordas da tela.
vec3 vinheta(vec3 cor, vec2 uv, float presenca) {
    vec2 c = uv - 0.5;
    return cor * (1.0 - presenca * 0.35 * smoothstep(0.2, 0.75, dot(c, c) * 2.0));
}

void main() {
    vec2 uv = texCoord;
    vec3 cena = texture(Sampler0, uv).rgb;
    vec4 vista = od_vista(Sampler1, InvProj, uv);
    bool ceu = vista.w < 0.5;
    vec3 rel = (InvView * vec4(vista.xyz, 1.0)).xyz;
    vec3 rd = normalize((InvView * vec4(normalize(vista.xyz), 0.0)).xyz);
    float dist = ceu ? 1.0e4 : length(rel);

    float presenca = od_suave(Fenda);
    float abre = od_suave(Abertura);

    // O mundo escurece e esfria.
    vec3 frio = mix(cena, od_cinza(cena), 0.45) * vec3(0.55, 0.60, 0.74);
    vec3 cor = mix(cena, frio, presenca * 0.75);

    vec3 D = normalize(OlhoDir);
    vec3 E = D * DISTANCIA;

    // Feixe fraco do olho até onde ele olha (só com o olho aberto).
    float s;
    float dFeixe = distRaioSegmento(rd, dist, E, Alvo, s);
    float largFeixe = mix(14.0, 3.5, s);
    float feixe = exp(-dFeixe * dFeixe / (largFeixe * largFeixe)) * abre * (0.25 + 0.75 * s);
    feixe *= 0.85 + 0.15 * sin(Tempo * 1.3 + s * 9.0);

    if (!ceu) {
        // Luz dourada fraca onde ele olha.
        float d = length(rel - Alvo);
        float luz = exp(-d * d / 70.0) * abre;
        float anel = 0.5 + 0.5 * sin(d * 1.4 - Tempo * 1.8);
        cor += cena * OURO_3 * luz * (0.9 + 0.3 * anel);
        cor += OURO_2 * feixe * 0.07;
        fragColor = vec4(vinheta(cor, uv, presenca), 1.0);
        return;
    }

    // Céu: escurece mais, para o olho aparecer.
    cor *= mix(1.0, 0.42, presenca);
    cor += OURO_2 * feixe * 0.05;

    float frente = dot(rd, D);
    if (frente > 0.25 && presenca > 0.001) {
        vec3 R = normalize(cross(D, vec3(0.0, 1.0, 0.0)));
        vec3 U = cross(R, D);
        vec2 q = vec2(dot(rd, R), dot(rd, U)) / frente;

        vec3 g = normalize(vec3(dot(Olhar, R), dot(Olhar, U), -dot(Olhar, D)));
        // A íris não sai do miolo da abertura.
        vec2 gxy = g.xy * vec2(1.0, 0.55);
        float lim = 0.6;
        if (length(gxy) > lim) gxy = normalize(gxy) * lim;
        g = normalize(vec3(gxy, sqrt(max(1.0 - dot(gxy, gxy), 0.0))));

        bool cima = q.y > 0.0;
        float h = meiaAltura(q.x, abre, cima);
        float u = clamp(1.0 - (q.x * q.x) / (LARGURA * LARGURA), 0.0, 1.0);
        float dy = abs(q.y) - h;            // < 0 dentro da abertura

        // Halo: o céu em volta da fenda fica escuro, com um calor dourado bem perto.
        vec2 e = q / vec2(LARGURA * 1.1, LARGURA * 0.5);
        float re = length(e);
        cor *= mix(1.0, 0.45, exp(-max(re - 0.8, 0.0) * 1.5) * presenca);
        cor += OURO_1 * exp(-max(re - 0.7, 0.0) * 4.0) * presenca * 0.22;

        // Rachaduras saindo da fenda.
        float aura;
        float rach = rachaduras(q, od_suave(Fenda * 1.1 - 0.1), aura);
        float pisca = 0.85 + 0.15 * sin(Tempo * 3.0 + q.x * 20.0);
        cor += OURO_2 * aura * presenca * 0.35 * pisca;
        cor = mix(cor, mix(OURO_3, vec3(1.0, 0.98, 0.9), rach * 0.5), rach * presenca);

        // Rasgo: borda escura e irregular em volta das pálpebras, com fio de brasa dourada.
        float palpebra = (0.012 + 0.03 * u) * (0.35 + 0.65 * abre);
        float rasgo = (0.008 + 0.022 * u + 0.012 * quebrado(q.x * 60.0, cima ? 1.0 : 2.0)) * presenca;
        float naBorda = step(abs(q.x), LARGURA * 1.06);
        if (dy > palpebra && dy < palpebra + rasgo && naBorda > 0.5) {
            float t = (dy - palpebra) / max(rasgo, 1e-4);
            vec3 vazio = vec3(0.012, 0.01, 0.008);
            cor = mix(vazio, cor, od_suave(t * t));
            cor += OURO_2 * exp(-(1.0 - t) * 18.0) * 0.9 * presenca;
        }

        // Costura da fenda: linha dourada que cresce do meio para as pontas antes de abrir.
        float comp = LARGURA * od_suave(Fenda * 1.5);
        float noComp = smoothstep(comp, comp * 0.85, abs(q.x));
        float costura = od_linha(q.y, 0.0015 + 0.003 * u) * noComp;
        float costuraBrilho = exp(-abs(q.y) * 160.0) * noComp * u;
        cor += (vec3(1.0, 0.95, 0.75) * costura * 1.5 + OURO_2 * costuraBrilho) * (1.0 - abre);

        // Pálpebras: carne magra e acinzentada, rugas paralelas à borda, aro molhado.
        if (dy > 0.0 && dy < palpebra && naBorda > 0.5) {
            float t = dy / palpebra;
            float ruga = od_ruido(vec2(q.x * 90.0, dy * 900.0 + od_ruido(q * 40.0) * 3.0));
            ruga = 0.6 + 0.5 * smoothstep(0.3, 0.7, ruga);
            vec3 carne = mix(vec3(0.34, 0.31, 0.30), vec3(0.17, 0.155, 0.15), t);
            carne *= ruga * mix(0.7, 1.0, od_fbm(q * 25.0));
            float molhado = exp(-dy * 900.0);
            carne = mix(carne, vec3(0.10, 0.08, 0.07), od_suave(1.0 - t * 6.0) * 0.6);
            carne += vec3(0.9, 0.8, 0.55) * molhado * 0.25;
            cor = mix(cor, carne, od_suave((1.0 - t) * 6.0) * presenca);
        }

        // Dentro: o globo.
        if (dy < 0.0 && abre > 0.002) {
            float emissao;
            vec3 olho = globo(q, g, dy, emissao);
            float bordaCanto = smoothstep(0.0, 0.006, -dy);
            cor = mix(cor, olho + OURO_3 * emissao * 0.45, bordaCanto);
        }
    }

    fragColor = vec4(vinheta(cor, uv, presenca), 1.0);
}
