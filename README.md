# Abyss Social - API
*By LONGELIN Bylel, NOTTEAU Romain, RIVIERE Axel, TRIPOGNEZ Quentin (BUT3 APP)*

---

Développement de l'architecture Backend d'un réseau social à l'aide de l'écosystème Spring, conçu pour alimenter l'interface d'une équipe frontend partenaire.

### Build the container image
```bash
podman build -t abyss-social-api:1.0 --arch=amd64 -f Containerfile .
```

### Run the container
```
podman run -d --name abyss-social-api -p 8080:8080 abyss-social-api:1.0
```
