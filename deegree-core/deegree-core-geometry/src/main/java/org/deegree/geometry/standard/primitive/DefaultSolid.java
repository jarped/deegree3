//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.geometry.standard.primitive;

import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.i18n.Messages;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Default implementation of {@link Solid}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultSolid extends AbstractDefaultGeometry implements Solid {

    private List<Surface> exteriorSurfaces;

    private List<Surface> interiorSurfaces;

    /**
     * Creates a new {@link DefaultSolid} instance from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param exteriorSurfaces
     *            the exterior surface (shell) of the solid, may be null
     * @param interiorSurfaces
     *            the interior surfaces of the solid, may be null or empty
     */
    public DefaultSolid( String id, ICRS crs, PrecisionModel pm, List<Surface> exteriorSurfaces,
                         List<Surface> interiorSurfaces ) {
        super( id, crs, pm );
        this.exteriorSurfaces = exteriorSurfaces;
        this.interiorSurfaces = interiorSurfaces;
    }

    @Override
    public int getCoordinateDimension() {
        return exteriorSurfaces.get( 0 ).getCoordinateDimension();
    }

    @Override
    public List<Surface> getExteriorSurface() {
        return exteriorSurfaces;
    }

    @Override
    public List<Surface> getInteriorSurfaces() {
        return interiorSurfaces;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Solid;
    }

    @Override
    public SolidType getSolidType() {
        return SolidType.Solid;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public Measure getArea( Unit requestedBaseUnit ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Measure getVolume( Unit requestedBaseUnit ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Envelope getEnvelope() {
        if ( env == null ) {
            if ( exteriorSurfaces != null ) {
                env = exteriorSurfaces.get( 0 ).getEnvelope();
            } else {
                for ( Surface interiorSurface : interiorSurfaces ) {
                    Envelope intEnv = interiorSurface.getEnvelope();
                    if ( env == null ) {
                        env = intEnv;
                    } else {
                        env = env.merge( intEnv );
                    }
                }
            }

        }
        return env;
    }
    
    @Override
    protected com.vividsolutions.jts.geom.Geometry buildJTSGeometry() {

        if ( exteriorSurfaces.size() < 1 || !( exteriorSurfaces.get( 0 ) instanceof Surface ) ) {
            throw new IllegalArgumentException( Messages.getMessage( "SOLID_HAS_NO_EXTERIOR" ) );
        }

        //// TODO handle the other patches as well
        //Surface surface = (Surface) exteriorSurfaces.get( 0 );
        //Ring exteriorRing = surface.getExteriorRing();
        //List<Ring> interiorRings = surface.getInteriorRings();

        //LinearRing shell = (LinearRing) getAsDefaultGeometry( exteriorRing ).getJTSGeometry();
        //LinearRing[] holes = null;
        //if ( interiorRings != null ) {
        //    holes = new LinearRing[interiorRings.size()];
        //    int i = 0;
        //    for ( Ring ring : interiorRings ) {
        //        holes[i++] = (LinearRing) getAsDefaultGeometry( ring ).getJTSGeometry();
        //    }
        //}
        String test=this.toString();
        //exteriorSurfaces.toString();
        return (Geometry) exteriorSurfaces;
    }
}