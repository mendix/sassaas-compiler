Example usage
```bash
curl https://sassaas.cfapps.io/v1/sass -X POST -F "template=@src/test/resources/template.zip"  -vv -F 'entrypoints=["sample.scss"]}' -F 'variables=[{"$primary-color": "#111"}]'
curl https://sassaas.cfapps.io/v1/sass -X POST -F "template=@/tmp/mendix-ui-theme-materialism.zip"  -vv -F 'entrypoints=["mendix-ui-theme-materialism/styles/sass/custom/custom.scss", "mendix-ui-theme-materialism/styles/sass/lib/lib.scss"]}' -F 'variables=[{"$color-default": "#111"}, {"$color-primary": "red"}]' -o out.zip
```
