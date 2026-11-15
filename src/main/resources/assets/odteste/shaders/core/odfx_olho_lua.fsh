#version 150

// Olho vermelho na lua (Sangue). Escrito do zero pela mesa Dev (desenho nosso, sem sigilo de obra nenhuma).
//
// Quando há Lua de Sangue (Enhanced Celestials) ou o jogador está na Vila Calada à noite, a lua vira um olho.
// Três desenhos para o Nikolas escolher (Variante):
//  0 "íris-lua": a lua inteira vira uma íris de carne vermelha, fibras que giram devagar, anel escuro na beira,
//    pupila preta redonda que respira; rachaduras finas de sangue saem da lua para o céu.
//  1 "pálpebras": pálpebras de carne escura em amêndoa por cima da lua; branco do olho manchado de veias,
//    íris vermelha, pupila em fenda (de bicho). Pisca de vez em quando.
//  2 "a lua que sangra": a lua fica quase igual, cor de ferida; só quando ele olha abre uma fenda de pupila com
//    um anel vermelho fino, e uma lágrima de sangue escorre da beira de baixo.
// A pupila olha para onde o Java manda (Olhar): quase sempre vagueia; raramente encara o jogador e segue devagar.
// Com profundidade: o olho só aparece no céu (nada pinta por cima de telhado). Sem profundidade (shader pack que
// não deixa ler): o olho pinta a lua clara do pack (vale lua vermelha/escura, como a Lua de Sangue do Enhanced
// Celestials) e, se o meio da lua está à vista, o disco do olho todo; o brilho em volta é bem fraco.
// A base do olho segue o "cima" da tela: a fenda fica em pé e a lágrima escorre para baixo mesmo com a lua no alto.

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InvProj;
uniform mat4 InvView;
uniform vec2 ScreenSize;
uniform float Tempo;
uniform float Forca;             // 0..1
uniform float ComProfundidade;
uniform vec3 LuaDir;             // direção da lua, eixos do mundo
uniform vec3 Olhar;              // para onde a pupila aponta, eixos do mundo (-LuaDir = encarando quem olha)
uniform float Abertura;          // 0..1: pálpebras (piscar) e abertura da fenda
uniform float Variante;          // 0, 1 ou 2
uniform vec3 LuaTela;            // xy: onde o meio da lua cai na tela; z: 1 se está na frente da câmera

in vec2 texCoord;
out vec4 fragColor;

#moj_import <odteste:odfx_comum.glsl>

const float RAIO = 0.125;         // raio do olho no plano da lua (o quadro da lua tem meia largura 0,2; o disco visível é menor)
const vec3 CARNE = vec3(0.30, 0.020, 0.025);
const vec3 CARNE_ESCURA = vec3(0.06, 0.004, 0.006);
const vec3 PRETO = vec3(0.010, 0.0, 0.002);

// Íris: fibras radiais vermelhas que giram devagar, criptas escuras, anel escuro na beira (limbo).
// s: 0 na pupila, 1 na beira da íris. a: ângulo.
vec3 iris(float s, float a, float viva, float limbo) {
    float fibra = od_ruido(vec2(a * 9.5 + Tempo * 0.03, s * 2.5)) * 0.55 + od_ruido(vec2(a * 26.0 - Tempo * 0.02, s * 5.0)) * 0.45;
    float cripta = smoothstep(0.68, 0.86, od_ruido(vec2(a * 13.0 + s * 3.0, s * 11.0 + 3.0))) * smoothstep(0.2, 0.45, s);
    vec3 c = mix(vec3(0.85, 0.12, 0.06), vec3(0.42, 0.02, 0.03), od_suave(s * 1.4));
    c *= 0.45 + 0.85 * fibra;
    c = mix(c, CARNE_ESCURA, cripta * 0.75);
    c += vec3(0.6, 0.08, 0.03) * exp(-abs(s - 0.12) * 28.0) * 0.6;    // anel vivo junto da pupila
    c = mix(c, PRETO * 2.0, od_suave((limbo - 0.80) * 5.0));           // limbo (anel escuro na beira)
    return c * viva;
}

// Esclera manchada (variante 1): branco sujo puxado para rosa, veias vermelhas vindo da beira.
vec3 esclera(vec2 p, float r) {
    float a = atan(p.y, p.x);
    vec3 c = mix(vec3(0.62, 0.42, 0.40), vec3(0.42, 0.20, 0.20), od_fbm3(p * 18.0));
    float veia = 0.0;
    for (int k = 0; k < 2; k++) {
        float fk = float(k);
        float v = 1.0 - abs(2.0 * od_ruido(vec2(a * (6.0 + fk * 7.0) + fk * 9.0, r * (14.0 + fk * 10.0))) - 1.0);
        veia = max(veia, pow(v, 22.0 + fk * 12.0));
    }
    c = mix(c, vec3(0.45, 0.02, 0.03), veia * smoothstep(0.3, 0.9, r / RAIO));
    return c * mix(0.45, 1.0, sqrt(max(1.0 - r * r / (RAIO * RAIO), 0.0)));   // bola: escurece na beira
}

// Rachaduras de sangue saindo da lua para o céu (variante 0) e a lágrima (variante 2).
float rachas(vec2 q, float r) {
    float a = atan(q.y, q.x);
    float n = 0.0;
    for (int i = 0; i < 7; i++) {
        float fi = float(i);
        float base = (fi + 0.4 * od_hash(vec2(fi, 3.0))) * (2.0 * OD_PI / 7.0);
        float comp = RAIO * (1.3 + 1.4 * od_hash(vec2(fi, 8.0)));
        float caminho = base + (od_ruido(vec2(r * 30.0, fi * 3.1)) - 0.5) * 0.35;
        float d = abs(mod(a - caminho + OD_PI, 2.0 * OD_PI) - OD_PI) * r;
        float fim = smoothstep(comp, RAIO, r) * step(RAIO * 0.98, r);
        n = max(n, od_linha(d, 0.0016 * (1.0 - (r - RAIO) / max(comp - RAIO, 1e-3)) + 0.0004) * fim);
    }
    return n;
}

// Claridade da lua contando o vermelho (a lua da Lua de Sangue é vermelha: pela luminância ela parece escura).
float od_claroLua(vec3 c) {
    return max(od_lum(c), c.r * 0.6);
}

void main() {
    vec2 uv = texCoord;
    vec3 cena = texture(Sampler0, uv).rgb;
    float k = clamp(Forca, 0.0, 1.0);
    if (k < 0.002) {
        fragColor = vec4(cena, 1.0);
        return;
    }
    vec3 rd = od_raio(InvProj, InvView, uv);
    vec3 l = normalize(LuaDir);
    float frente = dot(rd, l);
    if (frente < 0.6) {
        fragColor = vec4(cena, 1.0);
        return;
    }
    // base em volta da lua com o "cima" da tela (câmera): com a lua alta, o cima do mundo deitava o olho e a
    // lágrima virava um risco de lado
    vec3 cimaTela = normalize((InvView * vec4(0.0, 1.0, 0.0, 0.0)).xyz);
    vec3 bx = cross(cimaTela, l);
    bx = length(bx) > 1e-3 ? normalize(bx) : normalize(cross(vec3(0.0, 0.0, 1.0), l));
    vec3 by = cross(l, bx);
    vec2 q = vec2(dot(rd, bx), dot(rd, by)) / frente;
    float r = length(q);
    float a = atan(q.y, q.x);

    // onde pode pintar: com profundidade, só o céu; sem, só a lua clara do pack (e um brilho fraco em volta)
    float lum = od_lum(cena);
    float ceu, luaClara;
    if (ComProfundidade > 0.5) {
        ceu = od_vista(Sampler1, InvProj, uv).w < 0.5 ? 1.0 : 0.0;
        luaClara = ceu;
    } else {
        // "claro" conta o vermelho: a Lua de Sangue do Enhanced Celestials é vermelha e escura e não passava
        luaClara = smoothstep(0.12, 0.30, od_claroLua(cena));
        // o meio da lua à vista (5 amostras) libera o disco do olho inteiro, que é maior que a lua quadrada
        float vis = 0.0;
        if (LuaTela.z > 0.5) {
            vec2 px = 3.0 / ScreenSize;
            vis = max(od_claroLua(texture(Sampler0, LuaTela.xy).rgb),
                    max(max(od_claroLua(texture(Sampler0, LuaTela.xy + vec2(px.x, 0.0)).rgb),
                            od_claroLua(texture(Sampler0, LuaTela.xy - vec2(px.x, 0.0)).rgb)),
                        max(od_claroLua(texture(Sampler0, LuaTela.xy + vec2(0.0, px.y)).rgb),
                            od_claroLua(texture(Sampler0, LuaTela.xy - vec2(0.0, px.y)).rgb))));
            vis = smoothstep(0.12, 0.30, vis);
        }
        luaClara = max(luaClara, vis * (1.0 - smoothstep(RAIO * 0.97, RAIO * 1.02, r)));
        ceu = luaClara * 0.6 + 0.4 * smoothstep(0.0, 0.3, dot(rd, vec3(0.0, 1.0, 0.0)));
    }

    // pupila: deslocamento pelo Olhar (encarar = no meio), preso dentro da íris
    vec2 olha = vec2(dot(Olhar, bx), dot(Olhar, by));
    olha = (length(olha) > 1.0 ? normalize(olha) : olha) * RAIO * 0.42;
    float encara = 1.0 - smoothstep(0.02, 0.35, length(olha) / (RAIO * 0.42));
    float dil = 0.5 + 0.25 * sin(Tempo * 0.6 + sin(Tempo * 0.23) * 2.0) - 0.3 * encara;   // contrai quando encara
    vec2 p = q - olha;
    float rp = length(p);
    float ap = atan(p.y, p.x);
    float dentroDisco = 1.0 - smoothstep(RAIO * 0.97, RAIO * 1.02, r);
    float abre = od_suave(Abertura);
    int v = int(Variante + 0.5);
    vec3 cor = cena;
    float pinta = 0.0;            // quanto do olho entra aqui (0..1)
    vec3 olho = vec3(0.0);

    if (v == 0) {
        // íris-lua: a lua toda é íris (a pupila anda menos: a íris não sai do disco)
        p = q - olha * 0.55;
        rp = length(p);
        ap = atan(p.y, p.x);
        float raioIris = RAIO * 0.98;
        float pup = mix(0.18, 0.42, clamp(dil, 0.0, 1.0)) * raioIris;
        float s = clamp((rp - pup) / (raioIris - pup), 0.0, 1.0);
        olho = iris(s, ap, 1.0, r / raioIris);
        olho = mix(olho, PRETO, 1.0 - smoothstep(pup * 0.94, pup * 1.04, rp));
        olho *= mix(0.55, 1.0, sqrt(max(1.0 - r * r / (RAIO * RAIO), 0.0)));     // bola
        olho += vec3(0.25, 0.02, 0.02) * pow(max(1.0 - length(q - vec2(-0.06, 0.07)) / 0.03, 0.0), 2.0); // brilho úmido
        pinta = dentroDisco;
        // rachaduras de sangue no céu em volta
        float rach = rachas(q, r) * smoothstep(0.0, 1.0, k);
        cor = mix(cor, vec3(0.32, 0.015, 0.02), rach * ceu * 0.85);
    } else if (v == 1) {
        // pálpebras em amêndoa: a abertura cobre a lua; fora dela, carne escura
        float larg = RAIO * 1.05;
        float u = clamp(1.0 - q.x * q.x / (larg * larg), 0.0, 1.0);
        float meia = RAIO * 0.62 * abre * pow(u, 0.7) * (q.y > 0.0 ? 1.0 : 0.8);
        float dy = abs(q.y) - meia;                                  // < 0 dentro da abertura
        float raioIris = RAIO * 0.55;
        float s = clamp(rp / raioIris, 0.0, 1.0);
        // pupila em fenda vertical (abre mais quando não encara)
        float fenda = abs(p.x) / max(mix(0.06, 0.30, clamp(dil, 0.0, 1.0)) * raioIris * (1.0 - 0.6 * abs(p.y) / raioIris), 1e-4);
        vec3 ir = iris(s, ap, 1.0, s);
        ir = mix(ir, PRETO, (1.0 - smoothstep(0.9, 1.1, fenda)) * step(abs(p.y), raioIris * 0.95));
        vec3 dentro = mix(esclera(q, r), ir, 1.0 - smoothstep(raioIris * 0.96, raioIris * 1.03, rp));
        dentro *= mix(0.25, 1.0, smoothstep(0.0, 0.03, -dy) * (q.y > 0.0 ? 1.0 : 0.6) + (q.y > 0.0 ? 0.0 : 0.4));  // sombra da pálpebra
        // carne das pálpebras: rugas paralelas à borda, aro úmido
        float ruga = 0.6 + 0.4 * od_ruido(vec2(q.x * 90.0, dy * 400.0 + od_ruido(q * 30.0) * 2.0));
        vec3 carne = mix(CARNE * 0.8, CARNE_ESCURA, smoothstep(0.0, 0.05, dy)) * ruga;
        carne += vec3(0.25, 0.02, 0.02) * exp(-max(dy, 0.0) * 300.0) * 0.6;
        olho = dy < 0.0 ? dentro : carne;
        // a pálpebra é um pouco maior que a lua: some no céu numa beira rasgada
        float borda = RAIO * 1.25 + (od_ruido(vec2(a * 6.0, 1.0)) - 0.5) * 0.03;
        float fora = 1.0 - smoothstep(borda - 0.02, borda, length(q * vec2(0.92, 1.25)));
        pinta = max(dentroDisco * step(dy, 0.0), fora * step(0.0, dy));
    } else {
        // a lua que sangra: lua cor de ferida; a fenda abre quando ele olha; lágrima escorre
        vec3 ferida = mix(vec3(0.40, 0.04, 0.03), vec3(0.85, 0.20, 0.10), smoothstep(0.3, 0.9, lum)) * (0.7 + 0.3 * od_fbm3(q * 40.0));
        float raioIris = RAIO * 0.55 * (0.6 + 0.4 * abre);
        float anel = od_linha(rp - raioIris, 0.004) + (1.0 - smoothstep(raioIris * 0.9, raioIris, rp)) * 0.35;
        float fenda = abs(p.x) / max(mix(0.004, 0.035, abre) * (1.0 - 0.8 * pow(abs(p.y) / max(raioIris, 1e-3), 2.0)), 1e-4);
        float pupila = (1.0 - smoothstep(0.8, 1.2, fenda)) * step(abs(p.y), raioIris) * abre;
        olho = mix(ferida, vec3(0.55, 0.03, 0.03), clamp(anel, 0.0, 1.0) * abre);
        olho = mix(olho, PRETO, pupila);
        pinta = dentroDisco;
        // lágrima: sai da beira de baixo e escorre no céu, gota na ponta
        float desce = RAIO * (0.4 + 1.6 * od_suave(fract(Tempo * 0.04) * 1.3));
        float x = q.x - RAIO * 0.25 + (od_ruido(vec2(q.y * 25.0, 2.0)) - 0.5) * 0.01;
        float yTopo = -RAIO * 0.9;
        float fio = od_linha(x, 0.0025 + 0.0015 * smoothstep(yTopo, yTopo - desce, q.y)) * step(yTopo - desce, q.y) * step(q.y, yTopo);
        float gota = 1.0 - smoothstep(0.006, 0.009, length(vec2(x, q.y - (yTopo - desce))));
        cor = mix(cor, vec3(0.30, 0.012, 0.018), max(fio, gota) * ceu * k);
    }

    float m = pinta * k * (ComProfundidade > 0.5 ? ceu : luaClara);
    // a luz que a lua espalha no céu (halo do jogo ou o brilho/bloom do shader pack) também fica vermelha
    float espalha = (1.0 - smoothstep(RAIO, RAIO * 4.0, r)) * (ComProfundidade > 0.5 ? ceu : 1.0)
            * smoothstep(0.02, 0.15, lum) * k;
    cor = mix(cor, vec3(od_lum(cor)) * vec3(1.1, 0.10, 0.07), espalha);
    cor = mix(cor, olho, m);
    // brilho em volta: vermelho fraco, mais forte quando encara (sem profundidade, só um pouco)
    float halo = exp(-max(r - RAIO, 0.0) * 14.0) * (1.0 - dentroDisco) * (0.10 + 0.12 * encara) * k;
    cor += vec3(0.5, 0.02, 0.02) * halo * ceu * (ComProfundidade > 0.5 ? 1.0 : 0.5);
    fragColor = vec4(max(od_pontilhar(cor, uv * ScreenSize), 0.0), 1.0);
}
