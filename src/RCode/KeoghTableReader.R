data=read.table("../../data/ECGData/Figure_12_data.txt")
plot(unlist(data[,9]),type="l")
write.table(unlist(data[,9]),"../../in.txt",col.names=F,row.names=F)
