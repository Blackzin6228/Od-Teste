#version 150

// Visão do Marcado (Sangue). Escrito do zero pela mesa Dev.
//
// Quem carrega a Marca de Sangue vê o mundo por dentro de uma pálpebra. Camadas, de fora para dentro:
//  1) beira quase preta, de contorno orgânico (ruído torcido que se mexe devagar);
//  2) membrana vermelha translúcida: o mundo atrás fica vermelho e escuro (absorve, não pinta por cima),
//     com manchas de capilar; brilha por dentro a cada batida (a luz atravessa a parte fina);
//  3) veias em grupos (não uma rede igual na tela toda), que enchem na batida;
//  4) aberração cromática leve que cresce para a borda e na batida.
// Tudo pulsa junto com o coração. Cresce com o estágio da Marca. Não precisa de profundidade.

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InvProj;
uniform mat4 InvView;
uniform vec2 ScreenSize;
uniform float Tempo;
uniform float Forca;             // 0..1 (entrada, efeitos_fortes)
uniform float ComProfundidade;
uniform float Estagio;           // 0..1 (estágio da Marca / 4)
uniform float Pulso;             // 0..1: forma do batimento agora
uniform float Mexer;             // 0..1: distorção (efeitos_fortes: 1 / 0,5 / 0)

in vec2 texCoord;
out vec4 fragColor;

#moj_import <odteste:odfx_comum.glsl>

// Distância até a parede mais perto entre células (Voronoi): vira uma rede de vasos.
// "per" faz a volta fechar no ângulo (sem emenda no lado esquerdo da tela).
float vaso(vec2 p, float per) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float d1 = 8.0;
    float d2 = 8.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 g = vec2(float(x), float(y));
            vec2 cel = i + g;
            cel.x = mod(cel.x, per);
            vec2 o = vec2(od_hash(cel), od_hash(cel + 7.31));
            vec2 r = g + o - f;
            float d = dot(r, r);
            if (d < d1) {
                d2 = d1;
                d1 = d;
            } else if (d < d2) {
                d2 = d;
            }
        }
    }
    return sqrt(d2) - sqrt(d1);
}

// Veias que entram da borda para o meio: células compridas no sentido do raio (troncos) e uma rede
// mais fina por cima (galhos). Largura em pixels, para não virar mancha nem sumir em tela grande.
float veias(vec2 da, float px) {
    float ang = atan(da.y, da.x) / (2.0 * OD_PI) + 0.5;
    float rr = length(da);
    vec2 w = vec2(od_fbm3(da * 3.0 + 1.7), od_fbm3(da * 3.0 - 4.2)) - 0.5;
    vec2 q1 = vec2(ang * 30.0, rr * 2.6) + w * vec2(1.8, 0.5);
    vec2 q2 = vec2(ang * 70.0, rr * 7.0) + w * vec2(3.5, 1.0);
    float e1 = vaso(q1, 30.0);
    float e2 = vaso(q2, 70.0);
    float f1 = max(fwidth(e1), 1e-5);
    float l1 = 1.0 - smoothstep(0.0, f1 * px, e1);
    // sombra em volta do tronco (a veia fica embaixo da pele, não desenhada por cima)
    float s1 = (1.0 - smoothstep(0.0, f1 * px * 4.0, e1)) * 0.35;
    float l2 = 1.0 - smoothstep(0.0, max(fwidth(e2), 1e-5) * px * 0.45, e2);
    return max(max(l1, s1), l2 * 0.6);
}

void main() {
    vec2 uv = texCoord;
    float k = clamp(Forca, 0.0, 1.0) * clamp(Estagio, 0.0, 1.0);
    if (k < 0.002) {
        fragColor = texture(Sampler0, uv);
        return;
    }
    float asp = ScreenSize.x / max(ScreenSize.y, 1.0);
    vec2 d = uv - 0.5;
    vec2 da = vec2(d.x * asp, d.y);
    float p = clamp(Pulso, 0.0, 1.0);
    // distância até a borda (0 na borda, ~0.5 no meio), com a beira mordida por ruído torcido que se mexe devagar
    // (metade retângulo da tela, metade oval: a pálpebra é redonda, não uma moldura)
    float reta = min(min(uv.x, 1.0 - uv.x) * asp, min(uv.y, 1.0 - uv.y));
    float oval = (1.0 - length(da) / length(vec2(0.5 * asp, 0.5))) * 0.62;
    float borda = mix(reta, oval, 0.55);
    float carne = od_torcido(da * 3.2, Tempo * 0.06);
    borda += (carne - 0.5) * 0.11;
    // respira devagar e incha na batida
    float alcance = mix(0.11, 0.36, Estagio) * (1.0 + p * 0.16 + 0.04 * sin(Tempo * 0.8));
    float zona = 1.0 - smoothstep(0.0, alcance, borda);

    // distorção: a imagem incha para fora a cada batida e ondula perto da borda
    float onda = sin(Tempo * 1.7 + da.y * 9.0) * 0.5 + sin(Tempo * 1.1 - da.x * 7.0) * 0.5;
    vec2 desloca = d * (p * 0.010 + 0.002 * onda) * Estagio * Forca * Mexer;
    desloca += vec2(onda, -onda) * 0.0025 * zona * k * Mexer;
    vec2 uvD = uv - desloca;
    // aberração cromática: radial, mais forte na borda e na batida (o vermelho escapa para fora).
    // Fica um pouco mesmo sem tremor (é leve); o tremor só aumenta.
    float sep = (0.0012 + zona * (0.006 + p * 0.006)) * k * mix(0.4, 1.0, Mexer);
    vec3 c;
    c.r = texture(Sampler0, uvD + d * sep).r;
    c.g = texture(Sampler0, uvD).g;
    c.b = texture(Sampler0, uvD - d * sep * 0.7).b;

    // membrana: espessura cresce para a borda, manchada de capilar
    float capilar = od_fbm3(da * 9.0 + vec2(0.0, Tempo * 0.02));
    float esp = zona * zona * (1.4 + 1.6 * capilar) * k;
    vec3 atravessa = od_absorve(c, esp);
    // brilho por dentro: a luz atravessa a parte fina (meio da zona), mais na batida
    float fina = zona * (1.0 - zona) * 4.0;
    float mancha = smoothstep(0.35, 0.75, capilar);
    vec3 brilhoDentro = vec3(0.34, 0.020, 0.030) * fina * (0.2 + 0.8 * p) * (0.25 + 1.1 * mancha) * k;
    c = atravessa + brilhoDentro * (0.10 + od_lum(c) * 1.4);
    // beira quase preta (fora de tudo), com o contorno de carne
    float beira = 1.0 - smoothstep(0.0, alcance * 0.35, borda + (carne - 0.5) * 0.04);
    c = mix(c, vec3(0.018, 0.0, 0.003), beira * k * (0.85 + 0.15 * p));

    // veias em grupos: o ruído decide onde há feixe; ficam mais grossas perto da borda e enchem na batida
    float px = mix(0.6, 3.0, zona * zona) * (1.0 + p * 0.5) * (ScreenSize.y / 720.0);
    float grupo = smoothstep(0.38, 0.62, od_fbm3(da * 2.2 + 11.0));
    float v = veias(da, px) * mix(0.35, 1.0, grupo);
    float vMask = smoothstep(0.05, 0.9, zona) * k * (1.0 - beira * 0.6);
    vec3 sangueVeia = mix(vec3(0.030, 0.0, 0.006), vec3(0.22, 0.008, 0.02), p);
    c = mix(c, sangueVeia, v * vMask * 0.9);
    c += vec3(0.08, 0.0, 0.008) * v * vMask * (0.3 + 0.9 * p);

    fragColor = vec4(max(od_pontilhar(c, uv * ScreenSize), 0.0), 1.0);
}
