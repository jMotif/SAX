require(ggplot2)
require(reshape)
require(scales)
require(graphics)
require(grid)
require(gridExtra)
jet.colors <- colorRampPalette(c("#00007F", "blue", "#007FFF", "cyan",
                     "#7FFF7F", "yellow", "#FF7F00", "red", "#7F0000"))

jet.colors <- colorRampPalette(c("#00007F", "blue", "#007FFF", "cyan", "#7FFF7F", "yellow", "#FF7F00", "red", "#7F0000"))

#
#
dat = read.table("../resources/bitmap/ECGData/normal/normal_16.txt.shingled.txt",sep=",",quote="\'",header=T)
mat = matrix(unlist(dat),ncol=4,byrow=T)
mat.m = melt(rescale(mat))
p16 <- ggplot(mat.m, aes(X1, X2, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(7), guide = FALSE) + ggtitle("Normal 16")
p16
#
dat = read.table("../resources/bitmap/ECGData/normal/normal_18.txt.shingled.txt",sep=",",quote="\'",header=T)
mat = matrix(unlist(dat),ncol=4,byrow=T)
mat.m = melt(rescale(mat))
p18 <- ggplot(mat.m, aes(X1, X2, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(100), guide = FALSE) + ggtitle("Normal 18")
p18
#
dat = read.table("../resources/bitmap/ECGData/normal/normal_2.txt.shingled.txt",sep=",",quote="\'",header=T)
mat = matrix(unlist(dat),ncol=4,byrow=T)
mat.m = melt(rescale(mat))
p2 <- ggplot(mat.m, aes(X1, X2, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(100), guide = FALSE) + ggtitle("Normal 2")
p2
#
dat = read.table("../resources/bitmap/ECGData/normal/normal_4.txt.shingled.txt",sep=",",quote="\'",header=T)
mat = matrix(unlist(dat),ncol=4,byrow=T)
mat.m = melt(rescale(mat))
p4 <- ggplot(mat.m, aes(X1, X2, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(100)) + ggtitle("Normal 4")
p4
#
#
#p = rectGrob()
#grid.arrange(p, arrangeGrob(p,p,p, heights=c(3/4, 1/4, 1/4), ncol=1), ncol=2)
grid.arrange(p16,p18,p2,p4,ncol=2)


png("ECG_shingled.png", width=600, height=600)
grid.arrange(p16,p18,p2,p4,ncol=2)
dev.off()   
