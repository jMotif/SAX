library("devtools"); install_github("jmotif/jmotif-r",dependencies = TRUE)

# data
X <- c(-1, -2, -1, 0, 2, 1, 1, 0) 
p1 <- mean(X[1:4])
p2 <- mean(X[5:8])

sum = 0
for (i in c(1:4)) {
  sum = sum + dist(rbind(X[i], p1))
}
sum

sum = 0
for (i in c(5:8)) {
  sum = sum + dist(rbind(X[i], p2))
}
sum

jmotif::ts2string(X, 3)


x <- c(0, 0, 1, 1, 1, 1)
y <- c(1, 0, 1, 1, 0, 1)
dist(rbind(x, y))
