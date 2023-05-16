/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter.rsql;

import cz.jirutka.rsql.parser.ast.*;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FieldGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * visitor for traversing all nodes of an rsql query and returning a solr compliant query
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class SolrVisitor implements RSQLVisitor<String, Set<String>> {
  
  @Override
  public String visit(AndNode node, Set<String> fields) {
    return processChildNodes(node.getChildren(), Operator.AND, fields);
  }

  @Override
  public String visit(OrNode node, Set<String> fields) {
    return processChildNodes(node.getChildren(), Operator.OR, fields);
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
  
  private String processFieldGroup(final ComparisonNode node, final FieldGroup fieldGroup, final Set<String> fields, final StringBuilder builder) {
    final List<SearchField> fgf = fieldGroup.getFields();
    if(CollectionUtils.isEmpty(fgf)) {
      return "";
    }
    builder.append("(");
    for(int i = 0; i < fgf.size(); i++) {
      builder.append(processCompareNode(new ComparisonNode(node.getOperator(), fgf.get(i).getName(), node.getArguments()), fields));
      if(i != fgf.size() - 1) {
        builder.append(" OR ");
      }
    }
    builder.append(")");
    
    return builder.toString();
  }

  private String processCompareNode(final ComparisonNode node, Set<String> fields) {
    final StringBuilder builder = new StringBuilder();
    final FieldGroup fg = FieldGroup.getByName(node.getSelector());

    if(fg != null) {
      return processFieldGroup(node, fg, fields, builder);
    }
    final ComparisonOperator op = node.getOperator();

    List<String> arguments = node.getArguments();
    Iterator<String> argumentIterator = arguments.iterator();

    if (op.getSymbol().equals("!=")) {
      builder.append("-");
    }

    builder.append(node.getSelector());

    //ToDo match against SearchFields
    if (fields != null) {
      fields.add(node.getSelector());
    }

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

    switch (op.getSymbol()) {
      case "==":
      case "!=":
      case "=in=":
      case "=ge=":
      case "=gt=":
      case "=le=":
      case "=lt=": {
        builder.append(":");
        break;
      }
      default: {
        builder.append(" ");
        builder.append(op.getSymbol());
        builder.append(" ");
      }
    }

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
    builder.append(getOptionalBoosting(node.getSelector()));
    return builder.toString();
  }

  private String getOptionalQuotationMark(String argument) {
    return NumberUtils.isDigits(argument) || "*".equals(argument) ? "" : "\"";
  }

  private String getOptionalOpeningBracket(String symbol) {
    switch (symbol) {
      case "=in=":
        return "(";
      case "=ge=":
      case "=le=":
        return "[";
      case "=gt=":
      case "=lt=":
        return "{";
      default:
        return "";
    }
  }

  private String getOptionalClosingBracket(String symbol) {
    switch (symbol) {
      case "=in=":
        return ")";
      case "=ge=":
      case "=le=":
        return "]";
      case "=gt=":
      case "=lt=":
        return "}";
      default:
        return "";
    }
  }

  private String getOptionalBoosting(final String fieldName) {
    final SearchField searchField = SearchField.getByName(fieldName);
    if (searchField != null && searchField.getBoost() != null) {
      return "^" + searchField.getBoost();
    }
    return "";
  }
}
