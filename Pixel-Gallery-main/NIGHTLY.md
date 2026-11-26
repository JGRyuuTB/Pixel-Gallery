# Rama Nightly

La rama `nightly` es donde se prueban las novedades antes de pasar a `main` (estable).

## Cómo funciona

- La variante **Nightly** se instala junto a la estable: `applicationId` con sufijo `.nightly` y nombre "Pixel Gallery Nightly".
- Versión: `1.3.0-nightly.<número de ejecución>`. Cada compilación sube el `versionCode`, así se instala encima de la anterior.
- El workflow `.github/workflows/nightly.yml` compila con `gradle assembleNightly`:
  - cada noche a las 07:00 UTC (solo si hay commits nuevos),
  - en cada push a `nightly`,
  - a mano desde Actions > Nightly > Run workflow.
- El APK se publica como pre-release con la etiqueta `nightly-latest` (se reemplaza en cada compilación).
- La app Nightly busca actualizaciones en esa pre-release; la estable sigue usando la última release normal.

## Primeros pasos (Termux)

```bash
cd Galeria
git add . && git commit -m "Prepara la rama Nightly"
git push origin main            # el workflow programado debe existir en main
bash scripts/create-nightly-branch.sh
```

## Flujo de trabajo

```bash
git checkout nightly            # trabaja y prueba aquí
git add . && git commit -m "Describe el cambio" && git push

gh run download                 # baja el APK nightly para probarlo

git checkout main && git merge nightly && git push   # cuando esté listo
git tag v1.3.0 && git push origin v1.3.0             # publica la release estable
```
