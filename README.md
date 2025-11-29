# minriftSarath JMeter Project

## Local Run
```bash
export APP_COOKIE=<your MinBedriftSession cookie value>
mvn clean verify
```

- JMeter HTML dashboard will be generated in `results/dashboard/index.html`
- Sample GET request is configured to `https://test3-tgno.ujasri.net`

## GitLab CI/CD
- Go to **Settings → CI/CD → Variables**
- Add `APP_COOKIE` with your cookie value
- Commit & push the project
- Pipeline runs automatically
- HTML dashboard will be available as artifacts and optionally via GitLab Pages
