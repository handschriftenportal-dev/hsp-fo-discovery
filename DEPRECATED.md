# Deprecation-Log

## `numFound` - deprecated ab Version 0.6.0

Für https://projects.dev.sbb.berlin/issues/10747 wurde `numFound` in die zwei Felder `numGroupsFound` und `numDocsFound` aufgeteilt. `numFound` wird zwecks eindeutiger Benennung nicht weiterverwendet.

`de.staatsbibliothek.berlin.hsp.fo.discovery.application.response.ExtendedMetaData.numFound` (incl. Getter und Setter) wird ersetzt durch `de.staatsbibliothek.berlin.hsp.fo.discovery.application.response.ExtendedMetaData.numGroupsFound` (und dessen Getter und Setter). Gleiches gilt für `de.staatsbibliothek.berlin.hsp.fo.discovery.application.response.Response.MetaData.numFound`.
