data = read.table("../resources/test-data/ecg0606_1.csv")
dim(data)

dd = unlist(rbind(data,data))

par(mfrow = c(2,1))
plot(data$V1,type = "l")
plot(dd,type = "l")

win_size = 120

best_so_far_pos = -1
best_so_far_dist = 0
  
for (i in c(c(2232 + win_size):(length(dd) - win_size))) {
  
  print(paste(i, best_so_far_pos, best_so_far_dist))
  candidate_seq = dd[i:(i + win_size)]
  
  mindist = Inf
  
  for (j in c(1:(length(dd) - win_size))) {
    if (abs(j - i) > win_size) {
      distance = dist(rbind(candidate_seq, dd[j:(j + win_size)]))
      if (distance < mindist) {
        mindist <- distance
      }
    }
  }
  
  if (mindist > best_so_far_dist) {
    best_so_far_dist = mindist
    best_so_far_pos = i
  }
  
}

print(paste(best_so_far_pos, ", dist: ", best_so_far_dist))

