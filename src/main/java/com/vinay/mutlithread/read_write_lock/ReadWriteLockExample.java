package com.vinay.mutlithread.read_write_lock;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

public class ReadWriteLockExample {

    public static final int HIGHEST_PRICE = 1000;

    public static void main(String[] args) throws InterruptedException {
        InventoryDatabase inventoryDatabase = new InventoryDatabase();
        Random random = new Random();
        IntStream.range(0, 100000).forEach( i -> inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE)));
        Thread writer = new Thread(() -> {
            while (true){
                inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE));
                inventoryDatabase.removeItem(random.nextInt(HIGHEST_PRICE));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        writer.setDaemon(true);
        writer.start();

        int numberOfThreads = 7;
        List<Thread> readers = new ArrayList<>();
        IntStream.range(0, numberOfThreads).forEach(i -> {
            Thread reader = new Thread(()->{
                IntStream.range(0, 100000).forEach(j -> {
                    int upperBoundPrice = random.nextInt(HIGHEST_PRICE);
                    int lowerBoundPrice = upperBoundPrice > 0 ? random.nextInt(upperBoundPrice):0;
                    inventoryDatabase.getNumberOfItemInPriceRance(lowerBoundPrice, upperBoundPrice);

                });
            });
            reader.setDaemon(true);
            readers.add(reader);
        });

        long startReadingTime = System.currentTimeMillis();
        readers.forEach(Thread::start);
        for (Thread reader : readers) {
            reader.join();
        }
        long endReadingTime = System.currentTimeMillis();
        System.out.println(String.format("Reading took %d ms", endReadingTime- startReadingTime));

    }


    private static class  InventoryDatabase {

        private TreeMap<Integer, Integer> priceToCountMap = new TreeMap<>();
        private ReentrantLock lock = new ReentrantLock();
        private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        private Lock readLock = reentrantReadWriteLock.readLock();
        private Lock writeLock = reentrantReadWriteLock.writeLock();

        public int getNumberOfItemInPriceRance(int lowerBound, int upperBound){
            readLock.lock();
            try {
                Integer fromKey = priceToCountMap.ceilingKey(lowerBound);
                Integer toKey = priceToCountMap.floorKey(upperBound);

                if(fromKey == null || toKey == null){
                    return 0;
                }

                NavigableMap<Integer, Integer> rangeOfPrice = priceToCountMap.subMap(fromKey, true, toKey, true);

                return rangeOfPrice.values().stream().mapToInt(Integer::intValue).sum();
            } finally {
                readLock.unlock();
            }
        }

        public void addItem(int price){
            writeLock.lock();
            try {
                priceToCountMap.merge(price, 1, Integer::sum);
            } finally {
                writeLock.unlock();
            }
        }

        public void removeItem(int price){
            writeLock.lock();
            try {
                Integer numberOfItemsForPrice = priceToCountMap.get(price);
                if(numberOfItemsForPrice == null || numberOfItemsForPrice == 1){
                    priceToCountMap.remove(price);
                }else {
                    priceToCountMap.put(price, numberOfItemsForPrice-1);
                }
            } finally {
                writeLock.unlock();
            }
        }

    }
}
