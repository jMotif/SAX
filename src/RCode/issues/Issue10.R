install.packages("devtools")
library(devtools)
install_github('jMotif/jmotif-R')
library("jmotif")

png(filename = "issues/Issue10.png", width = 850, height = 650, units = "px", pointsize = 18,
    bg = "white", type = c("cairo", "cairo-png", "Xlib", "quartz"), antialias="subpixel")

x <- c(1,2,3,4,5,6,7)
paa4 <- paa(x,4)
plot(x, type="l", col="cyan", main="PAA Illustration"); points(x, col="cyan")
abline(h=x, lty=2, col="cyan"); abline(v=c(1:7), lty=2, col="cyan")
abline(v=c(1, 1+6/4, 1+12/4, 1+18/4, 1+24/4), lty=2, lwd=2, col="red")

abline(v=c(1+6/8, 1+18/8, 1+30/8, 1+42/8), lty=2, col="red")

lines(x=c(1+6/8, 1+18/8, 1+30/8, 1+42/8), y=paa4, type="l", col="blue", lwd=2)
points(x=c(1+6/8, 1+18/8, 1+30/8, 1+42/8), y=paa4, col="blue")

lines(x=c(1+6/8, 1+18/8, 1+30/8, 1+42/8), y=c(1.5, 3.5, 4.5, 6.5), type="l", col="green", lwd=2)
points(x=c(1+6/8, 1+18/8, 1+30/8, 1+42/8), y=c(1.5, 3.5, 4.5, 6.5), col="green")

legend("topleft", c("Time series", "PAA - jMotif", "PAA - Issue #10"), lty=c(1,1,1),lwd=c(1,2,2),col=c("cyan","blue", "green"))

dev.off()
