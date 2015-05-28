require(ggplot2)
require(reshape)
require(scales)
require(graphics)
jet.colors <- colorRampPalette(c("#00007F", "blue", "#007FFF", "cyan",
                     "#7FFF7F", "yellow", "#FF7F00", "red", "#7F0000"))
#
#
dat = read.table("../../out.txt",sep=",",quote="\'",header=T)
mat = matrix(unlist(dat),ncol=4,byrow=T)
mat.m = melt(rescale(mat))
p2 <- ggplot(mat.m, aes(X1, X2, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(7)) + ggtitle("data 2")
p2
#
#