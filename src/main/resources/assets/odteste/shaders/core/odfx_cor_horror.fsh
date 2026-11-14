#version 150

// Cor do horror (overworld inteiro, de dia também). Escrito do zero pela mesa Dev.
//
// O mundo fica morto e frio: quase sem cor (o verde vira oliva-cinza), um tom doente nos médios, sombras fundas,
// céu de chumbo baixo com nuvens escuras, uma névoa fria que engole a distância, grão leve e cantos escuros.
// Fica com cor só o que acende (tocha, lanterna, painel de máquina) e o vermelho vivo. À noite e nas áreas da história fica mais pesado. Não mexe na luz:
// quem escurece a noite é a camada de horror (mapa de luz); aqui é só a cor.
// Sem profundidade (shader pack que não deixa ler): o mesmo tom, com o céu achado pela direção e pela cor.

uniform sampler2D Sampler0;      // cor da cena
uniform sampler2D Sampler1;      // profundidade da cena
uniform mat4 InvProj;
uniform mat4 InvView;
uniform vec2 ScreenSize;
uniform float Tempo;             // segundos
uniform float Forca;             // 0..1 (overworld, efeitos_fortes, dá lugar ao carmesim)
uniform float ComProfundidade;   // 1 = profundidade boa; 0 = versão sem profundidade
uniform float Noite;             // 0..1
uniform float Area;              // 0..1 dentro de uma área da história

in vec2 texCoord;
out vec4 fragColor;

#moj_import <odteste:odfx_comum.glsl>

// Ar frio: cinza-chumbo de dia, quase preto de noite.
vec3 corDoAr(float noite) {
    return mix(vec3(0.27, 0.29, 0.30), vec3(0.026, 0.030, 0.036), noite);
}

// O tom do mundo: quase sem cor, verde morto, frio e doente, sombra funda, luz contida.
vec3 graduar(vec3 c, float p, float noite) {
    float l = od_lum(c);
    float viva = max(max(c.r, c.g), c.b);
    float croma = (viva - min(min(c.r, c.g), c.b)) / max(viva, 0.01);
    // luz que se acende (tocha, lanterna, fogo, painel de máquina) guarda a cor: o calor no meio do frio
    float acesa = smoothstep(mix(0.30, 0.16, noite), mix(0.65, 0.45, noite), viva) * smoothstep(0.25, 0.55, croma);
    // o vermelho vivo (sangue, redstone, flor) também fica
    float vermelho = smoothstep(0.45, 0.75, (c.r - max(c.g, c.b)) / max(c.r, 0.02)) * smoothstep(0.12, 0.3, c.r);
    float guarda = max(acesa * mix(0.30, 1.0, noite), vermelho * 0.85);
    // 1) verde vira oliva-cinza morto (grama, folha)
    float verde = smoothstep(0.02, 0.10, c.g - max(c.r, c.b));
    vec3 oliva = vec3(l) * vec3(1.02, 1.0, 0.80);
    vec3 r = mix(c, oliva, verde * 0.95 * p * (1.0 - guarda));
    // 2) cor: de dia sobra ~22% da saturação, de noite ~18%
    float sat = mix(mix(0.22, 0.18, noite), 0.92, guarda);
    r = mix(vec3(od_lum(r)), r, mix(1.0, sat, p));
    // 3) tom doente nos médios (esverdeado-amarelado frio), sombras frias
    float medio = smoothstep(0.06, 0.25, l) * smoothstep(0.75, 0.35, l);
    vec3 tom = mix(vec3(0.88, 0.95, 1.06), vec3(0.97, 1.0, 0.84), medio);
    r *= mix(vec3(1.0), tom, p * (1.0 - guarda * 0.9));
    // 4) sombra funda: o escuro desce mais que o claro (um pouco de leite frio só de dia)
    float ergue = mix(0.022, 0.006, noite) * p;
    vec3 fundo = pow(max(r, 0.0), vec3(1.0 + 0.30 * p * (1.0 - 0.6 * noite)));
    r = mix(r, fundo, 1.0 - guarda * 0.7);
    r = vec3(0.80, 0.92, 1.0) * ergue + r * (1.0 - ergue);
    // 5) luz contida: o branco não estoura e o dia fica mais baixo
    float topo = smoothstep(0.45, 1.0, l);
    r *= 1.0 - p * (0.16 + 0.16 * topo) * (1.0 - guarda * 0.6);
    return r;
}

// Céu: chumbo baixo e pesado; as nuvens ficam MAIS escuras que o céu (sem branco nenhum).
vec3 ceuCinza(vec3 c, float p, float noite) {
    float l = od_lum(c);
    float nuvem = smoothstep(0.72, 0.92, l);
    vec3 chumbo = vec3(0.30, 0.32, 0.33) - vec3(0.10, 0.10, 0.09) * nuvem;
    vec3 escuro = vec3(l * 0.6) * vec3(0.90, 0.95, 1.05);
    vec3 cinza = mix(chumbo, escuro, noite);
    return mix(c, cinza, p * 0.92);
}

// Grão de filme e cantos escuros.
vec3 acabamento(vec3 c, vec2 uv, float p, float noite) {
    float g = od_hash(uv * ScreenSize + fract(Tempo * 7.0) * 113.0) - 0.5;
    c += g * mix(0.030, 0.040, noite) * p * (0.35 + od_lum(c));
    vec2 d = uv - 0.5;
    c *= 1.0 - p * mix(0.34, 0.42, noite) * od_suave(dot(d, d) * 2.4);
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
    float noite = clamp(Noite, 0.0, 1.0);
    // de dia já é forte; noite e área da história levam ao máximo
    float p = k * mix(0.78, 1.0, max(noite, clamp(Area, 0.0, 1.0)));
    vec3 rd = od_raio(InvProj, InvView, uv);

    if (ComProfundidade > 0.5) {
        vec4 v = od_vista(Sampler1, InvProj, uv);
        if (v.w < 0.5) {
            vec3 r = ceuCinza(c, p, noite);
            r = graduar(r, p * 0.3, noite);
            fragColor = vec4(acabamento(r, uv, p, noite), 1.0);
            return;
        }
        float dist = length(od_rel(InvView, v.xyz));
        vec3 r = graduar(c, p, noite);
        // ar frio que engole a distância (mais curto de noite e nas áreas)
        float alcance = mix(0.030, 0.034, max(noite, Area * 0.6));
        float ar = (1.0 - exp(-dist * alcance)) * mix(0.70, 0.78, noite) * p;
        // o ar fica no tom do próprio céu ali (a névoa do jogo já existe: aqui só esfria e acinzenta)
        r = mix(r, corDoAr(noite), ar);
        fragColor = vec4(acabamento(r, uv, p, noite), 1.0);
        return;
    }

    // ---- sem profundidade: céu pela direção + cor (azul claro olhando para cima), ar pela altura do olhar
    float l = od_lum(c);
    float azul = smoothstep(0.02, 0.14, c.b - max(c.r, c.g * 0.92));
    float escuroNoAlto = smoothstep(0.10, 0.03, l) * noite;
    float ceu = smoothstep(0.0, 0.12, rd.y) * clamp(max(azul, escuroNoAlto) + smoothstep(0.75, 0.95, l) * 0.6, 0.0, 1.0);
    vec3 r = mix(graduar(c, p, noite), graduar(ceuCinza(c, p, noite), p * 0.3, noite), ceu);
    float horizonte = exp(-abs(rd.y) * 7.0) * (1.0 - ceu);
    r = mix(r, corDoAr(noite), horizonte * 0.28 * p);
    fragColor = vec4(acabamento(r, uv, p, noite), 1.0);
}
