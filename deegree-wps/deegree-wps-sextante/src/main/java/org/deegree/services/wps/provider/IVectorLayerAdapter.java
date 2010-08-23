//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.wps.provider;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.ValueRepresentation;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import es.unex.sextante.dataObjects.FeatureImpl;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;

/**
 * The IVectorLayerAdapter has methods to create a vector layer from a geometry, feature or feature collection and <br>
 * methods to create a geometry or feature collection from a vector layer.
 * 
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class IVectorLayerAdapter {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger( IVectorLayerAdapter.class );

    /**
     * Creates a IVectorLayer from a org.deegree.feature.FeatureCollection.
     * 
     * @param c
     *            feature collection
     * @return vector layer
     */
    public static IVectorLayer createVectorLayer( FeatureCollection c ) {

        // parameters for vector layer
        LinkedList<IFeature> features = new LinkedList<IFeature>();
        Field[] vectorLayerPropertyDeclarations = null;
        String crs = null;

        // property names
        LinkedList<QName> featurePropertyDeclarationNames = new LinkedList<QName>();

        Iterator<Feature> it = c.iterator();
        if ( it.hasNext() ) {

            // get feature type
            Feature f = it.next();
            FeatureType fType = f.getType();

            // get crs
            crs = createCRS( f );

            // get property declarations
            vectorLayerPropertyDeclarations = createPropertyDeclarationsForVectorLayer( fType );

            // traverse all features
            for ( Feature feature : c ) {

                if ( feature.getType().equals( fType ) ) {// check feature type

                    LOG.info( "FEATURE: " + feature.getId() );

                    // create properties
                    Object[] values = createPropertiesForVectorLayerGeometry( feature, vectorLayerPropertyDeclarations );

                    // create geometry
                    com.vividsolutions.jts.geom.Geometry geom = createJTSGeometry( feature );

                    // add feature
                    features.add( new FeatureImpl( geom, values ) );

                } else {
                    LOG.error( "Feature with id '" + feature.getId()
                               + "' have an other feature type as the others (not supported)." );
                    // TODO throw Exception
                }

            }
        }

        // create vector layer
        VectorLayerImpl layer = new VectorLayerImpl( "FeatureCollectionLayer", crs, vectorLayerPropertyDeclarations );

        // add features to the layer
        for ( IFeature f : features ) {
            layer.addFeature( f );
        }

        return layer;
    }

    /**
     * Creates a IVectorLayer from a org.deegree.feature.Feature.
     * 
     * @param f
     *            - feature
     * @return vector layer
     */
    public static IVectorLayer createVectorLayer( Feature f ) {

        // get crs
        String crs = createCRS( f );

        // create property declarations
        Field[] propertiyDeclarations = createPropertyDeclarationsForVectorLayer( f.getType() );

        // create properties
        Object[] properties = createPropertiesForVectorLayerGeometry( f, propertiyDeclarations );

        // create geometry
        com.vividsolutions.jts.geom.Geometry geom = createJTSGeometry( f );

        // create vector layer
        VectorLayerImpl layer = new VectorLayerImpl( "FeatureLayer", crs, propertiyDeclarations );
        layer.addFeature( new FeatureImpl( geom, properties ) );

        return layer;
    }

    /**
     * Creates a IVectorLayer from a org.deegree.geometry.Geometry.
     * 
     * @param g
     *            - geometry
     * @return vector layer
     */
    public static IVectorLayer createVectorLayer( Geometry g ) {

        // create vector layer
        VectorLayerImpl layer = new VectorLayerImpl( "GeometryLayer", g.getCoordinateSystem().getName() );

        // add geometry to layer
        layer.addFeature( createJTSGeometry( g ), null );

        return layer;
    }

    /**
     * Creates a org.deegree.feature.FeatureCollection from a vector layer.
     * 
     * @param l
     *            - vector layer
     * @return feature collection
     * @throws IteratorException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    public static FeatureCollection createFeatureCollection( IVectorLayer l )
                            throws IteratorException, IllegalArgumentException, InstantiationException,
                            IllegalAccessException {
        return createFeatureCollection( l, null );
    }

    /**
     * Creates a org.deegree.feature.FeatureCollection from a vector layer.
     * 
     * @param l
     *            - vector layer
     * @param ft
     *            - feature type
     * @return feature collection
     * @throws IteratorException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static FeatureCollection createFeatureCollection( IVectorLayer l, FeatureType ft )
                            throws IteratorException, IllegalArgumentException, InstantiationException,
                            IllegalAccessException {

        // list of features
        LinkedList<Feature> features = new LinkedList<Feature>();

        IFeatureIterator it = l.iterator();

        int idCounter = 0;

        if ( ft == null ) {// without feature type
            while ( it.hasNext() ) {
                features.add( createFeature( it.next(), "SextanteFeature" + ++idCounter, l ) );
            }
        } else {// with feature type
            while ( it.hasNext() ) {
                features.add( createFeature( it.next(), "SextanteFeature" + ++idCounter, l, ft ) );
            }
        }

        // create feature collection
        GenericFeatureCollection coll = new GenericFeatureCollection( "FeatureCollection", features );

        return coll;
    }

    /**
     * Creates a org.deegree.feature.Feature from a IFeature.
     * 
     * @param f
     *            - IFeature
     * @param id
     *            - feature id
     * @param l
     *            - vector layer
     * @return feature
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    private static Feature createFeature( IFeature f, String id, IVectorLayer l )
                            throws IllegalArgumentException, InstantiationException, IllegalAccessException {
        return createFeature( f, id, l, null );
    }

    /**
     * Creates a org.deegree.feature.Feature from a IFeature.
     * 
     * @param f
     *            - IFeature
     * @param id
     *            - feature id
     * @param l
     *            - vector layer
     * @param ft
     *            - feature type
     * @return feature
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static Feature createFeature( IFeature f, String id, IVectorLayer l, FeatureType ft )
                            throws IllegalArgumentException, InstantiationException, IllegalAccessException {

        Feature feature;

        if ( ft == null ) {// without feature type

            // create property declarations
            LinkedList<PropertyType> propDecls = new LinkedList<PropertyType>();

            // create simple properties
            for ( int i = 0; i < l.getFieldCount(); i++ ) {
                String[] name = l.getFieldName( i ).replace( "{", "" ).split( "}" );
                if ( name.length >= 2 ) {
                    QName probName = new QName( name[0], name[1] );

                    SimplePropertyType spt = new SimplePropertyType(
                                                                     probName,
                                                                     1,
                                                                     1,
                                                                     PrimitiveType.determinePrimitiveType( f.getRecord().getValues()[i] ),
                                                                     false, new LinkedList<PropertyType>() );

                    propDecls.add( spt );

                }
            }

            // create simple geometry
            GeometryPropertyType gpt = new GeometryPropertyType( new QName( "geom" ), 1, 1,
                                                                 GeometryType.MULTI_GEOMETRY,
                                                                 CoordinateDimension.DIM_2, false,
                                                                 new LinkedList<PropertyType>(),
                                                                 ValueRepresentation.INLINE );
            propDecls.add( gpt );

            // creatre feature type
            GenericFeatureType fty = new GenericFeatureType( new QName( "SextanteFeatureType" ), propDecls, false );

            // create properties
            LinkedList<Property> props = new LinkedList<Property>();
            Iterator<PropertyType> it = propDecls.iterator();
            Object[] propObjs = f.getRecord().getValues();
            for ( int i = 0; i < propObjs.length; i++ ) {
                if ( it.hasNext() ) {
                    // GenericProperty gp = new GenericProperty( it.next(), new PrimitiveValue( propObjs[i] ) );
                    SimpleProperty sp = new SimpleProperty( (SimplePropertyType) it.next(), propObjs[i].toString(),
                                                            PrimitiveType.determinePrimitiveType( propObjs[i] ) );
                    props.add( sp );
                }
            }
            if ( it.hasNext() ) {

                Geometry geom = createGeometry( f.getGeometry() );

                GenericProperty gp = new GenericProperty( it.next(), geom );
                props.add( gp );
            }

            // create feature
            feature = new GenericFeature( fty, id, props, GMLVersion.GML_31 );

        } else {// with feature type
            feature = null;
        }

        return feature;
    }

    /**
     * Creates a org.deegree.geometry.Geometry from a vector layer.
     * 
     * @param l
     *            - vector layer
     * @return geometry
     * @throws IteratorException
     */
    public static Geometry createGeometry( IVectorLayer l )
                            throws IteratorException {

        // JTS geometry
        com.vividsolutions.jts.geom.Geometry gJTS = null;

        IFeatureIterator it = l.iterator();

        if ( l.getShapesCount() == 1 ) { // only one shape in layer
            gJTS = it.next().getGeometry();

        } else { // more shapes in layer

            com.vividsolutions.jts.geom.GeometryFactory gFactoryJTS = new com.vividsolutions.jts.geom.GeometryFactory(
                                                                                                                       new PrecisionModel() );

            // create a JTS geometry array
            com.vividsolutions.jts.geom.Geometry[] gArrayJTS = new com.vividsolutions.jts.geom.Geometry[l.getShapesCount()];
            int k = 0;
            while ( it.hasNext() ) {
                gArrayJTS[k] = it.next().getGeometry();
                k++;
            }

            // create a JTS geometry collection
            gJTS = gFactoryJTS.createGeometryCollection( gArrayJTS );
        }

        // create a deegree geometry
        Geometry g = createGeometry( gJTS );

        return g;
    }

    /**
     * Creates a CRS-String from a feature.
     * 
     * @param f
     *            - feature
     * @return CRS-String
     */
    private static String createCRS( Feature f ) {

        String crs = null;
        Property[] geoms = f.getGeometryProperties();
        if ( geoms.length > 0 ) {
            Geometry g = (Geometry) geoms[0].getValue();
            crs = g.getCoordinateSystem().getName();
        }

        return crs;
    }

    /**
     * Creates com.vividsolutions.jts.geom.Geometry from org.deegree.feature.Feature.
     * 
     * @param f
     *            - feature
     * @return geometry
     */
    private static com.vividsolutions.jts.geom.Geometry createJTSGeometry( Feature f ) {

        Property[] fGeometries = f.getGeometryProperties();
        com.vividsolutions.jts.geom.Geometry geom;

        if ( fGeometries.length == 1 ) { // only one geometry

            geom = createJTSGeometry( (Geometry) fGeometries[0].getValue() );

        } else { // more geometries

            com.vividsolutions.jts.geom.GeometryFactory gFactoryJTS = new com.vividsolutions.jts.geom.GeometryFactory(
                                                                                                                       new PrecisionModel() );

            // create jts geometry array
            com.vividsolutions.jts.geom.Geometry[] geoms = new com.vividsolutions.jts.geom.Geometry[fGeometries.length];
            for ( int i = 0; i < fGeometries.length; i++ ) {
                geoms[i] = createJTSGeometry( (Geometry) fGeometries[i].getValue() );
            }

            // create a JTS geometry collection
            geom = gFactoryJTS.createGeometryCollection( geoms );

        }

        return geom;
    }

    /**
     * Creates com.vividsolutions.jts.geom.Geometry from org.deegree.geometry.Geometry.
     * 
     * @param g
     *            - geometry (deegree)
     * @return geometry (JTS)
     */
    private static com.vividsolutions.jts.geom.Geometry createJTSGeometry( Geometry g ) {

        // create jts geometry
        AbstractDefaultGeometry gAbst = (AbstractDefaultGeometry) g;
        com.vividsolutions.jts.geom.Geometry gJTS = gAbst.getJTSGeometry();

        return gJTS;
    }

    /**
     * Creates org.deegree.geometry.Geometry from com.vividsolutions.jts.geom.Geometry.
     * 
     * @param gJTS
     *            - geometry (JTS)
     * @return geometry (deegree)
     */
    private static Geometry createGeometry( com.vividsolutions.jts.geom.Geometry gJTS ) {

        // default deegree geometry to create a deegree geometry from JTS geometry
        GeometryFactory gFactory = new GeometryFactory();
        AbstractDefaultGeometry gDefault = (AbstractDefaultGeometry) gFactory.createPoint(
                                                                                           "GeometryFromIVectorLayerAdapter",
                                                                                           0, 0, CRS.EPSG_4326 );

        return gDefault.createFromJTS( gJTS );

    }

    /**
     * Creates a property declaration for a vector layer.
     * 
     * @param type
     *            - feature type
     * @return property declaration
     */
    private static Field[] createPropertyDeclarationsForVectorLayer( FeatureType type ) {

        LOG.info( "FEATURE TYP: " + type.getName().toString() );

        // list of property declaration for vector layer
        LinkedList<Field> vectoLayerPropertyDeclarations = new LinkedList<Field>();

        // get property declarations
        List<PropertyType> propertyDeclarations = type.getPropertyDeclarations();

        for ( PropertyType pt : propertyDeclarations ) {

            // handle only SimplePropertyType
            if ( pt instanceof SimplePropertyType ) {
                SimplePropertyType spt = (SimplePropertyType) pt;

                // name
                QName pName = spt.getName();

                // class
                Class<?> pClass = spt.getPrimitiveType().getValueClass();

                // notice name and class as field
                vectoLayerPropertyDeclarations.add( new Field( pName.toString(), pClass ) );

                LOG.info( "  PROPERTY: " + pName + ":   " + pClass );
            }
        }

        // create property declaration array
        int i = 0;
        Field[] fields = new Field[vectoLayerPropertyDeclarations.size()];
        for ( Field field : vectoLayerPropertyDeclarations ) {
            fields[i] = field;
            i++;
        }

        return fields;
    }

    /**
     * Creates properties for a geometry of a vector layer. <br>
     * Returns for multiple properties, the first property <br>
     * if names are set.
     * 
     * @param f
     *            - feature
     * @param propertyDeclarations
     *            - names of properties for return
     * @return properties
     */
    private static Object[] createPropertiesForVectorLayerGeometry( Feature f, Field[] propertyDeclarations ) {

        Object[] geomProperties;

        if ( propertyDeclarations != null ) {
            geomProperties = new Object[propertyDeclarations.length];

            for ( int i = 0; i < propertyDeclarations.length; i++ ) {

                String[] name = propertyDeclarations[i].getName().replace( "{", "" ).split( "}" );

                if ( name.length >= 2 ) {

                    QName probName = new QName( name[0], name[1] );

                    // propterties by name
                    Property[] properties = f.getProperties( probName );

                    // notice only the first
                    if ( properties.length >= 1 ) {

                        TypedObjectNode probNode = properties[0].getValue();

                        if ( probNode instanceof PrimitiveValue ) {
                            geomProperties[i] = ( (PrimitiveValue) properties[0].getValue() ).getValue();
                        } else {
                            LOG.warn( "Property '" + properties[0].getName() + "' is not supported." );
                            geomProperties[i] = null;
                        }

                    } else {
                        geomProperties[i] = null;
                    }

                    LOG.info( "  PROPERTY: " + propertyDeclarations[i].getName() + ": " + geomProperties[i] );
                }
            }

        } else { // if names=null
            geomProperties = new Object[] {};
        }

        return geomProperties;
    }



}
