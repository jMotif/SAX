library("jmotif")

png(filename = "issues/Issue10.png", width = 900, height = 650, units = "px", pointsize = 18,
    bg = "white", type = c("cairo", "cairo-png", "Xlib", "quartz"), antialias="subpixel")

x <- 1:7
paa4 <- jmotif::paa(x,4)
plot(x, type="l", col="blue", main="PAA Illustration", xlim = c(0,8), ylim = c(0,8))
points(x, col="blue")
abline(h=x, lty=2, col="cyan"); abline(v=c(1:7), lty=2, col="cyan")

inc <- 7/4
breaks <- seq(1-0.5,8,by = inc) 
paa_centers <- (breaks + (inc / 2))[1:4]
abline(v=breaks, lty=2, lwd=1.2, col="red") # segment breaks
abline(v=paa_centers, lty=3, col="red")
points(x=paa_centers, y=paa4, pch=8, cex=2, lwd=2, col="green")

lines(x=c(1+6/8, 1+18/8, 1+30/8, 1+42/8), y=c(1.5, 3.5, 4.5, 6.5), type="l", col="violet", lwd=2)
points(x=c(1+6/8, 1+18/8, 1+30/8, 1+42/8), y=c(1.5, 3.5, 4.5, 6.5), col="violet")

legend("topleft", c("Time series", "PAA - jMotif", "PAA - Issue #10"), 
    lty=c(1,1,1),lwd=c(1,2,2),col=c("blue","green", "violet"))

dev.off()
