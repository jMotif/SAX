library(jmotif)

x = 1:15 # series is 15 points long
inc = 15/7 # there shall be 7 segments

png(filename = "issues/Issue12.png", width = 900, height = 650, units = "px", pointsize = 18,
    bg = "white", type = c("cairo", "cairo-png", "Xlib", "quartz"), antialias="subpixel")


plot(x, type="l", xlim=c(0,16), ylim=c(0,16), col="cornflowerblue",
     main="Approximation distance computation"); 
points(x, col="cornflowerblue", cex=1.2)
breaks=seq(1-0.5,16,by=inc)
abline(v=breaks,lty=2,col="cyan")

paa7 = jmotif::paa(x, 7)
paa_centers <- (breaks + (inc / 2))[1:7]
points(x=paa_centers, y=paa7, pch=9, cex=0.7, col="brown")
segments(x0=breaks[1:7],x1=breaks[2:8],y0=paa7,y1=paa7,col="brown")

total = 0.
for (i in 0:14) {
  
  idx = floor((i+0.5)/inc)
  if(idx<0){idx=0}
  if(idx>6){idx=6}
  
  text(i+1-0.4,x[i+1]+0.45, idx, col="violet")
  
  segments(i+1,x[i+1],i+1,paa7[idx+1],col="green",lwd=3)
  dd = as.numeric(dist(t(cbind( c(x[i+1]), c(paa7[idx+1]) ))))
  total = total + dd
  
  text(i+1+0.5,x[i+1]-0.45, paste(round(total,3)), col="blue")
}
legend("topleft", 
       c("Assumed PAA index for original point",
         "Cumulative distance along original points",
         "Accounted distance segments"), 
       lty=c(1,1,1),
       lwd=c(3,3,3),col=c("violet","blue","green"))

text(7, 2, paste("Total dist:",total/length(x)), col="blue", adj=c(0,0))

dev.off()
