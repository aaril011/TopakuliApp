---
name: Pakuli Ethnobotany
colors:
  surface: '#fbf9f8'
  surface-dim: '#dbdad9'
  surface-bright: '#fbf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f3'
  surface-container: '#efeded'
  surface-container-high: '#e9e8e7'
  surface-container-highest: '#e4e2e2'
  on-surface: '#1b1c1c'
  on-surface-variant: '#3d4a41'
  inverse-surface: '#303031'
  inverse-on-surface: '#f2f0f0'
  outline: '#6d7a70'
  outline-variant: '#bccabe'
  surface-tint: '#006d43'
  primary: '#006d43'
  on-primary: '#ffffff'
  primary-container: '#00a669'
  on-primary-container: '#00311c'
  inverse-primary: '#5bde9b'
  secondary: '#59605d'
  on-secondary: '#ffffff'
  secondary-container: '#dbe1dd'
  on-secondary-container: '#5e6461'
  tertiary: '#8f4e00'
  on-tertiary: '#ffffff'
  tertiary-container: '#d37c21'
  on-tertiary-container: '#432200'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#79fbb5'
  primary-fixed-dim: '#5bde9b'
  on-primary-fixed: '#002111'
  on-primary-fixed-variant: '#005231'
  secondary-fixed: '#dee4e0'
  secondary-fixed-dim: '#c2c8c4'
  on-secondary-fixed: '#171d1b'
  on-secondary-fixed-variant: '#424846'
  tertiary-fixed: '#ffdcc2'
  tertiary-fixed-dim: '#ffb77a'
  on-tertiary-fixed: '#2e1500'
  on-tertiary-fixed-variant: '#6d3a00'
  background: '#fbf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e4e2e2'
typography:
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 28px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  container-margin: 20px
  gutter: 12px
---

## Brand & Style

The design system is rooted in the lush, organic essence of Pakuli Village. It balances traditional ecological knowledge with modern accessibility. The visual language is **Corporate / Modern** with strong **Minimalist** influences, prioritizing clarity and trust for a community-driven medicinal database.

The atmosphere is "Nurturing and Authoritative"—evoking the feeling of a digital herbarium. It utilizes a soft green-on-white palette to ensure legibility and a sense of cleanliness, essential for health-related information. The UI uses generous whitespace and structured containers to make complex biological data feel approachable.

## Colors

The palette is derived from the flora of Central Sulawesi. 

- **Primary (Forest Green):** Used for primary actions, headers, and active states. It represents growth and vitality.
- **Secondary (Mint Wash):** A very light green used for background surfaces and section containers to reduce visual fatigue.
- **Tertiary (Warm Amber):** Used sparingly for alerts, warnings, or high-priority notifications.
- **Neutrals:** A range of greys from deep charcoal for text (#333333) to soft strokes (#E0E0E0) for input borders.

The background is strictly white or the secondary mint wash to maintain a high-contrast, clinical yet friendly environment.

## Typography

The design system utilizes **Plus Jakarta Sans** for its friendly, modern, and highly legible geometric terminals. The type scale is optimized for information density, ensuring that plant names and preparation instructions are easily scannable.

- **Headlines:** Set in Bold/Semi-Bold weights to create a strong information hierarchy.
- **Body:** Standardized at 14px and 16px to ensure accessibility for a diverse age range within the village community.
- **Labels:** Used for form field descriptors and metadata (e.g., plant parts used).

## Layout & Spacing

This design system employs a **Fluid Grid** model with a base unit of 4px. 

### Mobile Layout
- **Margin:** 20px safe area on left and right.
- **Gutter:** 12px between cards or form elements.
- **Vertical Rhythm:** 16px (md) is the standard gap between related elements, while 24px (lg) separates distinct sections.

### Chat & Contribution Interface
The contribution feature uses a single-column layout to guide users through the multi-step "Submission" process without distraction. Information is grouped into cards to create "digestible chunks" of data.

## Elevation & Depth

Hierarchy is established through **Tonal Layers** and **Low-contrast outlines** rather than heavy shadows, keeping the UI light and fast.

1.  **Level 0 (Base):** White (#FFFFFF) or Secondary Green (#F4FAF6).
2.  **Level 1 (Cards):** White background with a 1px solid stroke (#E0E0E0) or a very soft, diffused ambient shadow (4px blur, 4% opacity).
3.  **Level 2 (Active/Floating):** Used for "Simpan" or "Lanjut" buttons, utilizing the Primary Green with high contrast against the background.

Form fields should appear slightly recessed using a light grey background (#F9F9F9) and a 1px inner stroke.

## Shapes

The shape language is **Rounded**, reflecting the organic nature of medicinal plants. 

- **Primary Buttons:** 0.5rem (8px) radius or full pill-shape for high-action items.
- **Cards/Containers:** 0.75rem (12px) to 1rem (16px) radius for a soft, welcoming feel.
- **Input Fields:** 0.5rem (8px) radius to maintain consistency with the buttons.
- **Selection Chips:** Full pill-shape (radius 999px) for easy tapping and visual distinction.

## Components

### Buttons
- **Primary:** Solid Primary Green with White text. Centered text, 16px horizontal padding.
- **Secondary:** Transparent background with Primary Green border and text.
- **Ghost:** Text-only with an arrow icon for "Kembali" actions.

### Form Inputs
Inputs use a light background fill to define the hit area. Labels are positioned above the field in `label-lg`. Required fields are marked with a red asterisk.

### Chips & Choice Tags
Used for selecting "Bagian tanaman" or "Peran". Inactive state: White background with a 1px border. Active state: Primary Green border with a light green fill and checkmark icon.

### Cards
All contribution types (recipes, plant benefits, corrections) are housed in cards with icons on the left and a trailing chevron to indicate navigation.

### Chat/Contribution Feature
For the new chat-based feature, use "Bubble" containers. User messages should be Primary Green with white text, while system/bot messages should be Light Grey with dark text to differentiate the "Expert/System" from the "Contributor."