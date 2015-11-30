
for(a_size in c(2:20)){
  rl=""
  inc = 1/a_size
  for(cc in c(1:(a_size))){
    rl=paste(rl,",",qnorm(cc*inc-inc/2),sep="")
  }
  print(rl)
}
