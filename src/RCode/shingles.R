library(data.table)
normal_2 = fread("../resources/bitmap/ECGData/normal/normal_2.txt")
normal_4 = fread("../resources/bitmap/ECGData/normal/normal_4.txt")
normal_6 = fread("../resources/bitmap/ECGData/normal/normal_6.txt")
normal_8 = fread("../resources/bitmap/ECGData/normal/normal_8.txt")
normal_16 = fread("../resources/bitmap/ECGData/normal/normal_16.txt")
normal_18 = fread("../resources/bitmap/ECGData/normal/normal_18.txt")

dd <- data.frame(normal2 = normal_2, normal4 = normal_4, normal6 = normal_6,
                 normal8 = normal_8, normal16 = normal_16, normal18 = normal_18)

library(ggplot2)
#
p1 <- ggplot(data=normal_2, 
       aes(x=1:length(normal_2$V1), y=V1)) + 
  geom_line(colour="cornflowerblue") + ggtitle("Normal 2") + theme_bw() +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  )
p1
#
p2 <- ggplot(data=normal_4, 
             aes(x=1:length(normal_4$V1), y=V1)) + 
  geom_line(colour="cornflowerblue") + ggtitle("Normal 4") + theme_bw() +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  )
p2
#
p3 <- ggplot(data=normal_6, 
             aes(x=1:length(normal_6$V1), y=V1)) + 
  geom_line(colour="cornflowerblue") + ggtitle("Normal 6") + theme_bw() +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  )
p3
#
p4 <- ggplot(data=normal_8, 
             aes(x=1:length(normal_8$V1), y=V1)) + 
  geom_line(colour="cornflowerblue") + ggtitle("Normal 8") + theme_bw() +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  )
p4
#
p5 <- ggplot(data=normal_16, 
             aes(x=1:length(normal_16$V1), y=V1)) + 
  geom_line(colour="cornflowerblue") + ggtitle("Normal 16") + theme_bw() +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  )
p5
#
p6 <- ggplot(data=normal_18, 
             aes(x=1:length(normal_18$V1), y=V1)) + 
  geom_line(colour="cornflowerblue") + ggtitle("Normal 18") + theme_bw() +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  )
p6
#
require(grid)
require(gridExtra)
grid.arrange(p1, p2, p3, p4, p5, p6,
             layout_matrix = rbind(c(5, 6), c(3, 4), c(1, 2)))
#
normal_2 <- as.numeric(fread("../resources/bitmap/ECGData/normal/normal_2.txt.shingled.txt"))
normal_4 <- as.numeric(fread("../resources/bitmap/ECGData/normal/normal_4.txt.shingled.txt"))
normal_6 <- as.numeric(fread("../resources/bitmap/ECGData/normal/normal_6.txt.shingled.txt"))
normal_8 <- as.numeric(fread("../resources/bitmap/ECGData/normal/normal_8.txt.shingled.txt"))
normal_16 <- as.numeric(fread("../resources/bitmap/ECGData/normal/normal_16.txt.shingled.txt"))
normal_18 <- as.numeric(fread("../resources/bitmap/ECGData/normal/normal_18.txt.shingled.txt"))
#
datas <- rbind(normal_2, normal_4, normal_6, normal_8, normal_16, normal_18)

dm = dist(datas)

hc <- hclust(distances, "ave")
plot(hc)

library(ggplot2)
library(ggdendro)
dendr <- dendro_data(hc, type="rectangle") 

#your own labels (now rownames) are supplied in geom_text() and label=label
pp1 <- ggplot() + 
  geom_segment(data=segment(dendr), aes(x=x, y=y, xend=xend, yend=yend)) + 
  ggtitle("Clustering of six datasets using shingling") +
  geom_text(data=label(dendr), aes(x=x, y=y, label=label, hjust=-0.2), size=5) +
  coord_flip() + scale_y_reverse(expand=c(0.2, 0)) + 
  theme(plot.title = element_text(size = rel(1.4)),
        axis.line.y=element_blank(),
        axis.ticks.y=element_blank(),
        axis.text.y=element_blank(),
        axis.title.y=element_blank(),
        axis.title.x=element_blank(),
        panel.background=element_rect(fill="white"),
        panel.grid=element_blank())
pp1

grid.arrange(pp,pp1, layout_matrix = rbind(c(1), c(2)))
#
#
#
#
library(scales)
jet.colors <-
  colorRampPalette(c("#00007F", "#007FFF", "cyan",
                     "#7FFF7F", "yellow", "#FF7F00", "#7F0000"))
#
dd <- data.frame(value=rescale(datas[1,]), x=rep(1:4,times=4), y=rep(1:4,each=4))
p1 <- ggplot(data=dd, aes(x, y, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(100), guide = FALSE) + theme_bw() +
  ggtitle("Normal 2") +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  ) 
p1
#
dd <- data.frame(value=rescale(datas[2,]), x=rep(1:4,times=4), y=rep(1:4,each=4))
p2 <- ggplot(data=dd, aes(x, y, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(100)) + theme_bw() +
  ggtitle("Normal 4") +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  ) 
p2
#
dd <- data.frame(value=rescale(datas[3,]), x=rep(1:4,times=4), y=rep(1:4,each=4))
p3 <- ggplot(data=dd, aes(x, y, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(100), guide = FALSE) + theme_bw() +
  ggtitle("Normal 6") +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  ) 
p3
#
dd <- data.frame(value=rescale(datas[4,]), x=rep(1:4,times=4), y=rep(1:4,each=4))
p4 <- ggplot(data=dd, aes(x, y, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(100)) + theme_bw() +
  ggtitle("Normal 8") +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  ) 
p4
#
dd <- data.frame(value=rescale(datas[5,]), x=rep(1:4,times=4), y=rep(1:4,each=4))
p5 <- ggplot(data=dd, aes(x, y, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(100), guide = FALSE) + theme_bw() +
  ggtitle("Normal 16") +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  ) 
p5
#
dd <- data.frame(value=rescale(datas[6,]), x=rep(1:4,times=4), y=rep(1:4,each=4))
p6 <- ggplot(data=dd, aes(x, y, fill=value)) + geom_tile() +
  scale_fill_gradientn(colours = jet.colors(100)) + theme_bw() +
  ggtitle("Normal 18") +
  theme(
    axis.title.x = element_blank(), axis.title.y = element_blank(),
    axis.text.x = element_blank(), axis.text.y = element_blank(),
    axis.ticks.x = element_blank(), axis.ticks.y = element_blank()
  ) 
p6
#
grid.arrange(p1, p2, p3, p4, p5, p6,
             layout_matrix = rbind(c(5, 6), c(3, 4), c(1, 2)),
             widths=c(3.3/7, 3.7/7))
