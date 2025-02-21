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
package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.rsql;

import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.*;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.Query2SolrQueryConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.StringHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * visitor for traversing all nodes of a RSQL query and returning a solr compliant query
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Component
public class SolrVisitor implements RSQLVisitor<String, Set<String>> {
  private final FieldProvider fieldProvider;
  private final Query2SolrQueryConverter query2SolrQueryConverter;

  @Autowired
  public SolrVisitor(final FieldProvider fieldProvider, final Query2SolrQueryConverter query2SolrQueryConverter) {
    this.fieldProvider = fieldProvider;
    this.query2SolrQueryConverter = query2SolrQueryConverter;
  }

  @Override
  public String visit(AndNode node, Set<String> fields) {
    return processChildNodes(node.getChildren(), Operator.AND, fields);
  }

  @Override
  public String visit(OrNode node, Set<String> fields) {
    final StringBuilder builder = new StringBuilder();
    builder.append("(");
    builder.append(processChildNodes(node.getChildren(), Operator.OR, fields));
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visit(ComparisonNode node, Set<String> fields) {
    return processCompareNode(node, fields);
  }

  private String processChildNodes(final List<Node> childNodes, final Operator operator, Set<String> fields) {
    int i;
    final StringBuilder builder = new StringBuilder();
    for (i = 0; i < childNodes.size(); i++) {
      builder.append(childNodes.get(i).accept(this, fields));

      if (i != childNodes.size() - 1) {
        builder.append(" ");
        builder.append(operator.getValue());
        builder.append(" ");
      }
    }
    return builder.toString();
  }

  private String processFieldGroup(final ComparisonNode node, final String fieldGroup, Set<String> fields, final StringBuilder builder) {
    final List<String> fgf = fieldProvider.getFieldNamesForGroup(fieldGroup);
    if(CollectionUtils.isEmpty(fgf)) {
      return "";
    }
    builder.append("(");
    for(int i = 0; i < fgf.size(); i++) {
      builder.append(processCompareNode(new ComparisonNode(node.getOperator(), fgf.get(i), node.getArguments()), fields));
      if(i != fgf.size() - 1) {
        builder.append(" OR ");
      }
    }
    builder.append(")");
    return builder.toString();
  }

  private String processCompareNode(final ComparisonNode node, Set<String> fields) {
    final StringBuilder builder = new StringBuilder();

    if(fieldProvider.groupExists(node.getSelector())) {
      return processFieldGroup(node, node.getSelector(), fields, builder);
    }
    if(!fieldProvider.isValid(node.getSelector())) {
      throw new RSQLParserException(new Throwable(String.format("Given field name %s is not valid.", node.getSelector())));
    }

    final ComparisonOperator op = node.getOperator();
    final List<String> arguments = node.getArguments();
    final Iterator<String> argumentIterator = arguments.iterator();
    final String fieldNameWithBoosting = fieldProvider.getFieldNameWithBoosting(node.getSelector());

    if(fields != null) {
      fields.add(fieldNameWithBoosting);
    }

    if(("==".equals(op.getSymbol()) || "!=".equals(op.getSymbol())) && StringHelper.isText(arguments.get(0))) {
      return processTextComparison(op, arguments.get(0), node.getSelector());
    }
    if (op.getSymbol().equals("!=")) {
      builder.append("-");
    }
    builder.append(node.getSelector());

    /*
     * field=="value" => field:"value"
     * field!="value" => field:-"value"
     * field=le=value => field:[* TO "value"]
     * field=lt=value => field:{* TO "value"}
     * field=ge=value => field:["value" TO *]
     * field=gt=value => field:{"value" TO *}
     * field=le=123 => field:[* TO 123]
     * field=in=[value1,value2,value3] => field:("value1" OR "value2" OR "value3")
     */
    builder.append(getOperator(op));
    builder.append(getOptionalOpeningBracket(op.getSymbol()));
    while (argumentIterator.hasNext()) {
      String argument = argumentIterator.next();

      if (op.getSymbol().equals("=lt=") || op.getSymbol().equals("=le=")) {
        builder.append("* TO ");
      }

      /* for numeric comparison do not add the quotation marks */
      builder.append(getOptionalQuotationMark(argument));
      builder.append(argument);
      builder.append(getOptionalQuotationMark(argument));

      if (op.getSymbol().equals("=gt=") || op.getSymbol().equals("=ge=")) {
        builder.append(" TO *");
      }
      if (argumentIterator.hasNext()) {
        builder.append(" OR ");
      }
    }
    builder.append(getOptionalClosingBracket(op.getSymbol()));
    builder.append(fieldProvider.getBoosting(node.getSelector()));
    return builder.toString();
  }

  private String processTextComparison(final ComparisonOperator op, final String argument, final String searchField) {
    final boolean isNegotiated = "!=".equals(op.getSymbol());
    return query2SolrQueryConverter.convert(argument, List.of(searchField), isNegotiated, true).getQuery();
  }

  private String getOptionalQuotationMark(String argument) {
    return NumberUtils.isDigits(argument) || "*".equals(argument) ? "" : "\"";
  }

  private String getOptionalOpeningBracket(String symbol) {
    return switch (symbol) {
      case "=in=" -> "(";
      case "=ge=", "=le=" -> "[";
      case "=gt=", "=lt=" -> "{";
      default -> "";
    };
  }

  private String getOptionalClosingBracket(String symbol) {
    return switch (symbol) {
      case "=in=" -> ")";
      case "=ge=", "=le=" -> "]";
      case "=gt=", "=lt=" -> "}";
      default -> "";
    };
  }

  private String getOperator(final ComparisonOperator op) {
    switch (op.getSymbol()) {
      case "==", "!=", "=in=", "=ge=", "=gt=", "=le=", "=lt=" -> {
        return ":";
      }
      default -> {
        return String.format(" %s ", op.getSymbol());
      }
    }
  }
}