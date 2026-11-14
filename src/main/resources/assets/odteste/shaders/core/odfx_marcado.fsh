#version 150

// Visão do Marcado (Sangue). Escrito do zero pela mesa Dev.
//
// Quem carrega a Marca de Sangue vê o mundo por dentro de uma pálpebra: veias escuras entram pelas bordas da
// tela e se ramificam, enchem a cada batida do coração e esvaziam devagar; a borda fica quente e úmida, a imagem
// treme e se separa um pouco em vermelho onde as veias passam. Cresce com o estágio da Marca. Não precisa
// de profundidade (o mesmo nos dois caminhos).

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
    // distância até a borda (0 na borda, ~0.5 no meio), com a borda "mordida" pelo ruído
    float borda = min(min(uv.x, 1.0 - uv.x) * asp, min(uv.y, 1.0 - uv.y));
    borda += (od_fbm3(da * 4.0 + Tempo * 0.03) - 0.5) * 0.08;
    float alcance = mix(0.10, 0.34, Estagio) * (1.0 + Pulso * 0.12);
    float zona = 1.0 - smoothstep(0.0, alcance, borda);

    // distorção: a imagem incha para fora a cada batida e ondula perto das veias
    float onda = sin(Tempo * 1.7 + da.y * 9.0) * 0.5 + sin(Tempo * 1.1 - da.x * 7.0) * 0.5;
    vec2 desloca = d * (Pulso * 0.010 + 0.002 * onda) * Estagio * Forca * Mexer;
    desloca += vec2(onda, -onda) * 0.0025 * zona * k * Mexer;
    vec2 uvD = uv - desloca;
    // separação de cor nas bordas (o vermelho escorre para fora)
    float sep = zona * k * (0.004 + Pulso * 0.004) * Mexer;
    vec3 c;
    c.r = texture(Sampler0, uvD + d * sep).r;
    c.g = texture(Sampler0, uvD).g;
    c.b = texture(Sampler0, uvD - d * sep * 0.5).b;

    // borda quente e escura (como olhar por dentro da pálpebra)
    float lum = od_lum(c);
    vec3 quente = vec3(lum * 0.9 + 0.025, lum * 0.12, lum * 0.10);
    c = mix(c, quente, zona * k * 0.6);
    c *= 1.0 - zona * k * (0.30 + Pulso * 0.20);

    // veias: só na zona da borda, mais grossas perto dela; enchem com o pulso
    float px = mix(0.6, 3.0, zona * zona) * (1.0 + Pulso * 0.5) * (ScreenSize.y / 720.0);
    float v = veias(da, px);
    float vMask = smoothstep(0.05, 0.9, zona) * k;
    // veia: quase preta, vinho por dentro; quando enche, o vinho aparece
    vec3 sangueVeia = mix(vec3(0.035, 0.0, 0.006), vec3(0.20, 0.008, 0.02), Pulso);
    c = mix(c, sangueVeia, v * vMask * 0.95);
    // no escuro a veia ainda se vê: um vermelho fraco por dentro, mais forte quando enche
    c += vec3(0.075, 0.0, 0.008) * v * vMask * (0.35 + 0.9 * Pulso);

    fragColor = vec4(max(c, 0.0), 1.0);
}
