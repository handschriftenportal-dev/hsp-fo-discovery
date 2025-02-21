/*
 * MIT License
 *
 * Copyright (c) 2021 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.InfoStats;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.BaseServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.staatsbibliothek.berlin.hsp.fo.discovery.util.MapHelper;

@RequestMapping("/stats")
@RestController
@Tag(name = "Stats")
public class StatsController {

    @AllArgsConstructor
    @Getter
    private enum STATUS {
        EXTERN("extern"),
        INTERN("intern");

        private final String value;
    }

    protected final BaseService<Void> statsService;

    public StatsController(final BaseService<Void> statsService) {
        this.statsService = statsService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InfoStats> getStats() {
        final Map<String, Long> kodCounts = findFacetCount(HspType.HSP_OBJECT);
        final long kodSum = getSum(kodCounts);

        final Map<String, Long> digitizedCounts = findFacetCount(HspType.HSP_DIGITIZED);
        final long digitizedSum = getSum(digitizedCounts);

        final Map<String, Long> catalogCounts = findFacetCount(HspType.HSP_CATALOG);
        final long catalogSum = getSum(catalogCounts);

        final Map<String, Long> retroDescriptionsCounts = findFacetCount(HspType.HSP_DESCRIPTION_RETRO);
        final long retroSum = getSum(retroDescriptionsCounts);
        final Map<String, Long> descriptionExternalCounts = findFacetCount(Map.of(FacetField.TYPE, List.of(HspType.HSP_DESCRIPTION.getValue()), FacetField.DESCRIPTION_STATUS, Collections.singletonList(STATUS.EXTERN.value)));
        final long descriptionExternSum = getSum(descriptionExternalCounts);
        final Map<String, Long> descriptionInternalCounts = findFacetCount(Map.of(FacetField.TYPE, List.of(HspType.HSP_DESCRIPTION.getValue()), FacetField.DESCRIPTION_STATUS, Collections.singletonList(STATUS.INTERN.value)));
        final long descriptionInternSum = getSum(descriptionInternalCounts);
        final Map<String, Long> mergedDescriptionInstitutions = MapHelper.mergeMaps(retroDescriptionsCounts, descriptionExternalCounts, descriptionInternalCounts);
        final long descriptionAllSum = getSum(mergedDescriptionInstitutions);

        final InfoStats stats = InfoStats.builder()
            .withKod(InfoStats.BaseStats.builder()
                .withInstitution(kodCounts)
                .withAll(kodSum)
                .build()
            )
            .withDigitized(InfoStats.BaseStats.builder()
                .withInstitution(digitizedCounts)
                .withAll(digitizedSum)
                .build()
            )
            .withCatalog(InfoStats.BaseStats.builder()
                .withInstitution(catalogCounts)
                .withAll(catalogSum)
                .build()
            )
            .withDescription(InfoStats.DescriptionStats.builder()
                    .withAll(descriptionAllSum)
                    .withRetro(retroSum)
                    .withExtern(descriptionExternSum)
                    .withIntern(descriptionInternSum)
                    .withInstitution(mergedDescriptionInstitutions)
                    .build()
            )
            .build();


        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    private Map<String, Long> findFacetCount(@NonNull final HspType... types) {
        final Map<FacetField, List<String>> filter = Stream.of(types).collect(Collectors.toMap(k -> FacetField.TYPE, v -> Stream.of(v.getValue()).toList()));
        return findFacetCount(filter);
    }

    private Map<String, Long> findFacetCount(final Map<FacetField, List<String>> filter) {
        final BaseService.SearchParams searchParams = BaseService.SearchParams.builder()
            .withFacetMinCount(1)
            .withFacets(List.of(FacetField.REPOSITORY_ID.getName()))
            .withFacetTermsExcluded(Collections.emptyList())
            .withFilterQueries(BaseServiceImpl.generateFilters(filter))
            .withIncludeMissingFacet(true)
            .withRows(0)
            .build();
        final MetaData metaData = statsService.findMetaData(searchParams);
        return metaData.getFacets()
            .get(FacetField.REPOSITORY_ID.getName());
    }

    private long getSum(final Map<String, Long> counts) {
        long sum;
        sum = counts.values().stream().mapToLong(l -> l).sum();
        return sum;
    }
}