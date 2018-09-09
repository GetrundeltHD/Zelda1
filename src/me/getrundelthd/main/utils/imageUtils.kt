package me.getrundelthd.main.utils

import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage


fun flip(bimage: BufferedImage): BufferedImage {
    var bufferedImage = bimage.getSubimage(0, 0, bimage.width, bimage.height)
    val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
    tx.translate((-bufferedImage.getWidth(null)).toDouble(), 0.0)
    val op = AffineTransformOp(tx,
            AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
    bufferedImage = op.filter(bufferedImage, null)
    return bufferedImage
}