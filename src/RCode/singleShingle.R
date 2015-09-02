require(ggplot2)
require(reshape)
require(scales)
require(graphics)
require(matlab)
jet.colors <- jet.colors(8)
#
#
dat = read.table("../resources/bitmap/normal_2_shingled.txt",sep=",",quote="\'",header=T)
mat = matrix(unlist(dat),ncol=4,byrow=F)
mat.m = melt(rescale(mat))
p2 <- ggplot(mat.m, aes(X1, X2, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(7)) + ggtitle("TEK14")
p2
#
#
png("TEK14_shingled.png", width=600, height=600)
print(p2)
dev.off()   
