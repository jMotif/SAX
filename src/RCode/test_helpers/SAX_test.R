library(seewave)
library(matlab)

# data 
x=c(-1, -2, -1, 0, 2, 1, 1, 0) 
plot(x,type="l")
points(x,pch=16,lwd=5)
#segments
abline(v=c(1,1+7/3,1+7/3*2,8),lty=3,lwd=2) 

# PAA implementation in seewave
l <- length(x)
PAA_number = 3
PAA <- array(0, PAA_number)
for (i in 1:PAA_number) {
  PAA[i] <- mean(x[round((i - 1) * l/PAA_number + 1):round(i * l/PAA_number)])
  print(paste("segment",i,"[",      # a debug output
              round((i - 1) * l/PAA_number + 1),
              ":",round(i * l/PAA_number),"] ->",
              PAA[i]))
}
points(x=seq(1+7/6,8,by=7/3),y=PAA,pch=3,lwd=3,col="red")

# PAA variant
paa <- function(ts, ap){
  len <- ncol(ts)
  res <- ts
  if(len != ap){
    if( (len %% ap) == 0 ){
      res <- reshape(ts, len %/% ap, ap)
    }else{
      tmp <- matrix(rep(ts,ap),byrow=T,nrow=ap)
      res <- reshape(tmp, len, ap)
    }
  }
  matrix(colMeans(res), nrow=1, ncol=ap)
}
paa1=paa(t(x),3)
points(x=seq(1+7/6,8,by=7/3),y=paa1,pch=3,lwd=5,col="blue")

# difference
paa1-t(PAA)


SAX
