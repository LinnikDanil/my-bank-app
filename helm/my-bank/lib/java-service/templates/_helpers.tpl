{{- define "java-service.name" -}}
{{- if .Values.nameOverride }}{{ .Values.nameOverride }}{{ else }}{{ .Chart.Name }}{{ end -}}
{{- end -}}
