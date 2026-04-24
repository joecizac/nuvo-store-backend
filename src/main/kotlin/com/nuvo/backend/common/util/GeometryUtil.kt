package com.nuvo.backend.common.util

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel

object GeometryUtil {
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    fun createPoint(latitude: Double, longitude: Double): Point {
        return geometryFactory.createPoint(Coordinate(longitude, latitude))
    }
}
