library(data.table)
#
dd1 = fread("../resources/dataset/TEK/TEK14.txt") 
#
sliding_wsize <- 100
win_count <- length(dd1$V1) - sliding_wsize
stdev_arr <- rep(0, win_count)
for (i in 1:win_count) {
  arr <- dd1$V1[i:(i + sliding_wsize)]
  stdev_arr[i] <- sd(arr)
}
#
summary(stdev_arr)
which(stdev_arr == min(stdev_arr))
plot(dd1$V1, type = "l", col = "cornflowerblue")
lines(x = 1238:(1238 + 100), y = dd1$V1[1238:(1238 + 100)], col = "red", lwd = 3)
lines(x = 800:(800 + 100), y = dd1$V1[800:(800 + 100)], col = "green", lwd = 3)
stdev_arr[780]
lines(x = 8:(8 + 100), y = dd1$V1[8:(8 + 100)], col = "orange", lwd = 3)
lines(x = 1466:(1466 + 100), y = dd1$V1[1466:(1466 + 100)], col = "violet", lwd = 3)
lines(x = 1104:(1104 + 100), y = dd1$V1[1104:(1104 + 100)], col = "black", lwd = 3)
#
tek_df <- data.frame(TEK14 = stdev_arr)
#
dd2 = fread("../resources/dataset/TEK/TEK16.txt") 
#
sliding_wsize <- 100
win_count <- length(dd2$V1) - sliding_wsize
stdev_arr2 <- rep(0, win_count)
for (i in 1:win_count) {
  arr <- dd2$V1[i:(i + sliding_wsize)]
  stdev_arr2[i] <- sd(arr)
}
#
tek_df$TEK16 <- stdev_arr2
#
dd3 = fread("../resources/dataset/TEK/TEK17.txt") 
#
sliding_wsize <- 100
win_count <- length(dd3$V1) - sliding_wsize
stdev_arr3 <- rep(0, win_count)
for (i in 1:win_count) {
  arr <- dd3$V1[i:(i + sliding_wsize)]
  stdev_arr3[i] <- sd(arr)
}
#
tek_df$TEK17 <- stdev_arr3
#
library(ggplot2)
dm <- melt(tek_df)
p1 <- ggplot(data = dm, aes(value, color = variable)) + geom_density()
p1

which(stde)