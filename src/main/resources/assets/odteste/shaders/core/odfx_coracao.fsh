#version 150

// Pulso do coração (Sangue): o Faminto está perto. Escrito do zero pela mesa Dev.
//
// A cada batida a tela "bate": puxa um pouco para dentro (zoom curto), o escuro fecha das bordas para o meio,
// a cor some por um instante e o canto fica vermelho de pressão; entre as batidas, volta devagar.
// Quanto mais perto, mais rápido (o Java marca o ritmo junto com o som) e mais forte. Sem profundidade.

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
    float r = length(vec2(d.x * asp, d.y)) / (0.5 * sqrt(asp * asp + 1.0)); // 0 no meio, 1 no canto

    // a tela puxa para dentro na batida (zoom curto) e um pouco de borrão radial nas bordas
    float zoom = 1.0 - b * k * 0.022 * Mexer;
    vec2 uvZ = 0.5 + d * zoom;
    vec3 c = vec3(0.0);
    float borrao = b * k * 0.012 * r * Mexer;
    for (int i = 0; i < 5; i++) {
        float f = float(i) / 4.0;
        c += texture(Sampler0, 0.5 + d * (zoom - borrao * f)).rgb;
    }
    c /= 5.0;

    // a cor some na batida
    float lum = od_lum(c);
    c = mix(c, vec3(lum) * vec3(1.0, 0.9, 0.9), b * k * 0.6);

    // escuro que fecha das bordas: sempre um pouco (medo), muito na batida
    float raio = mix(1.15, 0.55, k * (0.35 + 0.65 * b));
    float escuro = od_suave((r - raio * 0.45) / (raio * 0.75));
    c *= 1.0 - escuro * mix(0.55, 0.95, b) * k;
    // pressão: vermelho nos cantos na batida
    c += vec3(0.07, 0.0, 0.004) * smoothstep(0.55, 1.0, r) * b * k * (1.0 - escuro * 0.5);
    // a tela inteira apaga um pouco no "tum"
    c *= 1.0 - b * k * 0.22;

    fragColor = vec4(max(c, 0.0), 1.0);
}
