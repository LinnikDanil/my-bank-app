{{- define "logstash.name" -}}
{{- default .Chart.Name .Values.app.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}
