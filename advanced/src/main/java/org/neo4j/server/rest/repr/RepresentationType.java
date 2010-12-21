/**
 * Copyright (c) 2002-2010 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.server.rest.repr;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;

public final class RepresentationType
{
    private static final Map<String, RepresentationType> types = new HashMap<String, RepresentationType>();
    private static final Map<Class<?>, RepresentationType> extended = new HashMap<Class<?>, RepresentationType>();
    // Graph database types
    public static final RepresentationType
            GRAPHDB = new RepresentationType( "graphdb", null, GraphDatabaseService.class ),//
            NODE = new RepresentationType( "node", "nodes", Node.class ),//
            RELATIONSHIP = new RepresentationType( "relationship", "relationships", Relationship.class ),//
            PATH = new RepresentationType( "path", "paths", Path.class ),//
            RELATIONSHIP_TYPE = new RepresentationType( "relationship-type", "relationship-types" ),//
            PROPERTIES = new RepresentationType( "properties" ),//
            INDEX = new RepresentationType( "index" ),//
            NODE_INDEX_ROOT = new RepresentationType( "node-index" ),//
            RELATIONSHIP_INDEX_ROOT = new RepresentationType( "relationship-index" ),//
            PLUGINS = new RepresentationType( "plugins" ),//
            PLUGIN = new RepresentationType( "plugin" ),//
            PLUGIN_DESCRIPTION = new RepresentationType( "plugin-point" ),//
            SERVER_PLUGIN_DESCRIPTION = new RepresentationType( "server-plugin", null ),//
            PLUGIN_PARAMETER = new RepresentationType( "plugin-parameter", "plugin-parameter-list" ),//
            // Value types
            URI = new RepresentationType( "uri", null ),//
            TEMPLATE = new RepresentationType( "uri-template" ),//
            STRING = new RepresentationType( "string", "strings", String.class ),//
            // primitives
            BYTE = new RepresentationType( "byte", "bytes", Byte.class ),//
            CHAR = new RepresentationType( "character", "characters", Character.class ),//
            SHORT = new RepresentationType( "short", "shorts", Short.class ),//
            INTEGER = new RepresentationType( "integer", "integers", Integer.class ),//
            LONG = new RepresentationType( "long", "longs", Long.class ),//
            FLOAT = new RepresentationType( "float", "floats", Float.class ),//
            DOUBLE = new RepresentationType( "double", "doubles", Double.class ),//
            BOOLEAN = new RepresentationType( "boolean", "booleans", Boolean.class ),//
            NOTHING = new RepresentationType( "void", null ),//
            // System
            EXCEPTION = new RepresentationType( "exception" );

    final String valueName;
    final String listName;
    final Class<?> extend;

    private RepresentationType( String valueName, String listName )
    {
        this( valueName, listName, null );
    }

    private RepresentationType( String valueName, String listName, Class<?> extend )
    {
        this.valueName = valueName;
        this.listName = listName;
        this.extend = extend;
        if ( valueName != null ) types.put( valueName.replace( "-", "" ), this );
        if ( extend != null ) extended.put( extend, this );
    }

    RepresentationType( String type )
    {
        if ( type == null ) throw new IllegalArgumentException( "type may not be null" );
        this.valueName = type;
        this.listName = type+"s";
        this.extend = null;
    }

    @Override
    public String toString()
    {
        return valueName;
    }

    static RepresentationType valueOf( Class<? extends Number> type )
    {
        return types.get( type.getSimpleName().toLowerCase() );
    }

    @Override
    public int hashCode()
    {
        if ( valueName == null ) return listName.hashCode();
        return valueName.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof RepresentationType )
        {
            RepresentationType that = (RepresentationType) obj;
            if ( this.valueName != null )
            {
                if ( valueName.equals( that.valueName ) )
                {
                    if ( this.listName != null )
                    {
                        return listName.equals( that.listName );
                    }
                    else
                    {
                        return that.listName == null;
                    }
                }
            }
            else if ( this.listName != null )
            {
                return that.valueName == null && listName.equals( that.listName );
            }
        }
        return false;
    }

    static RepresentationType extended( Class<?> extend )
    {
        return extended.get( extend );
    }
}
