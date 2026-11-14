#version 150

// Pulso do coração (Sangue): o Faminto está perto. Escrito do zero pela mesa Dev.
//
// A cada batida (no mesmo tique do som: o Java zera a batida quando toca o "tum") a tela bate:
//  - puxa um pouco para dentro (zoom curto) com um borrão radial que separa as cores (4 amostras só);
//  - o escuro fecha das bordas, mas a beira dele ONDULA (ruído que corre em volta) e não é um círculo liso;
//  - na beira do escuro fica uma faixa de pressão vermelho-sangue, manchada, que brilha no "tum";
//  - a cor do meio some por um instante. Entre as batidas, volta devagar.
// Quanto mais perto, mais rápido e mais forte. Sem profundidade.

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InvProj;
uniform mat4 InvView;
uniform vec2 ScreenSize;
uniform float Tempo;
uniform float Forca;             // 0..1: quão perto (e efeitos_fortes)
uniform float ComProfundidade;
uniform float Batida;            // 0..1: forma do batimento agora
uniform float Mexer;             // 0..1: zoom e borrão (efeitos_fortes: 1 / 0,5 / 0)

in vec2 texCoord;
out vec4 fragColor;

#moj_import <odteste:odfx_comum.glsl>

void main() {
    vec2 uv = texCoord;
    float k = clamp(Forca, 0.0, 1.0);
    if (k < 0.002) {
        fragColor = texture(Sampler0, uv);
        return;
    }
    float b = clamp(Batida, 0.0, 1.0);
    float asp = ScreenSize.x / max(ScreenSize.y, 1.0);
    vec2 d = uv - 0.5;
    vec2 da = vec2(d.x * asp, d.y);
    float r = length(da) / (0.5 * sqrt(asp * asp + 1.0)); // 0 no meio, 1 no canto
    float ang = atan(da.y, da.x);

    // ondulação da beira: ruído que corre em volta da tela (fecha a volta sem emenda: lido num círculo)
    vec2 volta = vec2(cos(ang), sin(ang));
    float ond = od_fbm3(volta * 2.2 + vec2(Tempo * 0.25, -Tempo * 0.18) + b * 0.6) - 0.5;

    // a tela puxa para dentro na batida; a borda ondula um pouco junto (sutil, segue o tremor)
    float zoom = 1.0 - b * k * 0.022 * Mexer;
    float empurra = ond * 0.010 * smoothstep(0.45, 1.0, r) * (0.3 + b) * k * Mexer;
    float borrao = b * k * 0.012 * r * Mexer;
    // 4 amostras ao longo do raio; o vermelho pesa mais nas de fora e o azul nas de dentro (separa as cores)
    vec3 c = vec3(0.0);
    vec3 peso = vec3(0.0);
    for (int i = 0; i < 4; i++) {
        float f = float(i) / 3.0;
        vec3 w = vec3(0.4 + f, 1.0, 1.4 - f);
        c += texture(Sampler0, 0.5 + d * (zoom - borrao * f + empurra)).rgb * w;
        peso += w;
    }
    c /= peso;

    // a cor some na batida
    float lum = od_lum(c);
    c = mix(c, vec3(lum) * vec3(1.0, 0.9, 0.9), b * k * 0.55);

    // escuro que fecha das bordas: sempre um pouco (medo), muito na batida; a beira ondula
    float raio = mix(1.15, 0.55, k * (0.35 + 0.65 * b));
    float rr = r + ond * (0.05 + 0.05 * b) * k;
    float escuro = od_suave((rr - raio * 0.45) / (raio * 0.75));
    // faixa de pressão: logo antes do escuro, vermelho manchado (veias de pressão), brilha no "tum"
    float faixa = smoothstep(0.0, 0.45, escuro) * (1.0 - smoothstep(0.45, 1.0, escuro));
    float mancha = od_fbm3(da * 7.0 + volta * 0.5 + vec2(0.0, Tempo * 0.05));
    vec3 pressao = vec3(0.10, 0.003, 0.008) * faixa * (0.35 + 0.65 * smoothstep(0.3, 0.8, mancha)) * (0.25 + 0.75 * b) * k;
    c *= 1.0 - escuro * mix(0.55, 0.95, b) * k;
    c = od_absorve(c, faixa * (0.6 + 0.8 * mancha) * k) + pressao;
    // a tela inteira apaga um pouco no "tum"
    c *= 1.0 - b * k * 0.20;

    fragColor = vec4(max(od_pontilhar(c, uv * ScreenSize), 0.0), 1.0);
}
