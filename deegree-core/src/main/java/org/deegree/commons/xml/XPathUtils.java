//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.commons.xml;

import java.util.HashSet;
import java.util.Set;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.expr.BinaryExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.PathExpr;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.Step;
import org.jaxen.expr.UnaryExpr;
import org.jaxen.expr.VariableReferenceExpr;

/**
 * Utilitiy methods for common tasks that involve XPath expressions.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class XPathUtils {
    
    /**
     * Returns the namespace prefixes that are used in the given XPath 1.0 expression.
     * 
     * @param text
     *            xpath expression, must not be <code>null</code>
     * @return namespace prefixes used in the expresssion, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the given expression does not denote a valid XPath 1.0 expression
     */
    public static Set<String> extractPrefixes( String text ) {
        try {
            return extractPrefixes( new BaseXPath( text, null ).getRootExpr() );
        } catch ( JaxenException e ) {
            String msg = "'" + text + "' does not denote a valid XPath 1.0 expression: " + e.getMessage();
            throw new IllegalArgumentException( msg );
        }
    }

    /**
     * Returns the namespace prefixes that are used in the given XPath 1.0 expression.
     * 
     * @param xpath
     *            xpath expression, must not be <code>null</code>
     * @return namespace prefixes used in the expresssion, never <code>null</code>
     */
    public static Set<String> extractPrefixes( Expr xpath ) {
        Set<String> prefixes = new HashSet<String>();
        extractPrefixes( xpath, prefixes );
        return prefixes;
    }

    private static void extractPrefixes( Expr expr, Set<String> prefixes ) {
        if ( expr instanceof BinaryExpr ) {
            extractPrefixes( ( (BinaryExpr) expr ).getLHS(), prefixes );
            extractPrefixes( ( (BinaryExpr) expr ).getRHS(), prefixes );
        } else if ( expr instanceof FilterExpr ) {
            extractPrefixes( ( (FilterExpr) expr ).getExpr(), prefixes );
            for ( Object pred : ( (FilterExpr) expr ).getPredicates() ) {
                extractPrefixes( (Predicate) pred, prefixes );
            }
        } else if ( expr instanceof FunctionCallExpr ) {
            extractPrefix( ( (FunctionCallExpr) expr ).getPrefix(), prefixes );
            for ( Object param : ( (FunctionCallExpr) expr ).getParameters() ) {
                extractPrefixes( (Expr) param, prefixes );
            }
        } else if ( expr instanceof LocationPath ) {
            for ( Object step : ( (LocationPath) expr ).getSteps() ) {
                extractPrefixes( (Step) step, prefixes );
            }
        } else if ( expr instanceof PathExpr ) {
            extractPrefixes( ( (PathExpr) expr ).getFilterExpr(), prefixes );
            extractPrefixes( ( (PathExpr) expr ).getLocationPath(), prefixes );
        } else if ( expr instanceof UnaryExpr ) {
            extractPrefixes( ( (UnaryExpr) expr ).getExpr(), prefixes );
        } else if ( expr instanceof VariableReferenceExpr ) {
            extractPrefix( ( (VariableReferenceExpr) expr ).getPrefix(), prefixes );
        }
    }

    private static void extractPrefixes( Step step, Set<String> prefixes ) {
        if ( step instanceof NameStep ) {
            extractPrefix( ( (NameStep) step ).getPrefix(), prefixes );
        }
    }

    private static void extractPrefixes( Predicate pred, Set<String> prefixes ) {
        extractPrefixes( pred.getExpr(), prefixes );
    }

    private static void extractPrefix( String prefix, Set<String> prefixes ) {
        if ( prefix != null ) {
            prefixes.add( prefix );
        }
    }
}