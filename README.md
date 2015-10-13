Example usage
```bash
curl https://sassaas.cfapps.io/v1/sass -X POST -vv -F 'entrypoints=[{"mendix-ui-theme-silverlinings/styles/sass/custom/custom.scss": "mendix-ui-theme-silverlinings/styles/css/custom/custom.css"}, {"mendix-ui-theme-silverlinings/styles/sass/lib/lib.scss": "mendix-ui-theme-silverlinings/styles/css/lib/lib.css"}]}' -F 'variables=[{"$color-default": "#111"}, {"$color-primary": "red"}]' -o out.zip
curl https://sassaas.cfapps.io/v1/sass -X POST -vv -F "template=@/tmp/mendix-ui-theme-materialism.zip" -F 'entrypoints=[{"mendix-ui-theme-materialism/styles/sass/custom/custom.scss": "mendix-ui-theme-materialism/styles/css/custom/custom.css"}, {"mendix-ui-theme-materialism/styles/sass/lib/lib.scss": "mendix-ui-theme-materialism/styles/css/lib/lib.css"}]}' -F 'variables=[{"$color-default": "#111"}, {"$color-primary": "red"}]' -o out.zip
```
