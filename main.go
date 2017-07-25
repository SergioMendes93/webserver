package main

import (
	"time"
	"fmt"
	"os/exec"
	"sync"
	"strings"
	"bytes"
	"bufio"
	"os"
	"log"
	"strconv"
	"github.com/stats"
)

const MILLIS_IN_SECOND = 1000

var scheduled = 0
var notScheduled = 0

func worker(request int64, completeCh chan time.Duration, requests [][]string) {
	var i int64
	for i = 0; i < request; i++ {
		start := time.Now()

		//cmd := exec.Command("docker","-H", "tcp://10.5.60.2:2377","run", "-itd", "-c", "1000", "-m", "500000000", "-e", "affinity:makespan==3000","-e", "affinity:port==10000", "ubuntu")
		var out, stderr bytes.Buffer
		
		makespan := requests[i][0]
		cpu := requests[i][1]
		memory := requests[i][2]
		requestClass := requests[i][3]
		requestType := requests[i][4]
		image := requests[i][5]
		//requestRate := requests[i][6]
		portNumber := requests[i][7]

		if requestClass == "0" { //for other scheduling algorithms
			if requestType == "service" {
              			if image == "redis" {
					cmd := exec.Command("docker", "-H", "tcp://10.5.60.2:2377", "run", "-itd", "-p", portNumber +":"+ portNumber, "-c", cpu, "-m", memory, "-e", "affinity:makespan==", makespan, "-e", "affinity:port==", portNumber, image, "--port", portNumber)
					cmd.Stdout = &out
					cmd.Stderr = &stderr

					if err := cmd.Run(); err != nil {
						fmt.Println("Not scheduled")
						notScheduled++
						fmt.Println(fmt.Sprint(err) + ": " + stderr.String())
					} else {
						//TODO: se eu usar concorrencia entao vou precisar de usar locks para estes contadores
						scheduled++
					}
					completeCh <- time.Since(start)
				} else {
					cmd := exec.Command("docker", "-H", "tcp://10.5.60.2:2377", "run", "-itd", "-p", portNumber +":"+ portNumber, "-c", cpu, "-m", memory, "-e", "affinity:makespan==", makespan, "-e", "affinity:port==", portNumber, image, portNumber)
					cmd.Stdout = &out
					cmd.Stderr = &stderr

					if err := cmd.Run(); err != nil {
						fmt.Println("Not scheduled")
						notScheduled++
						fmt.Println(fmt.Sprint(err) + ": " + stderr.String())
					} else {
						//TODO: se eu usar concorrencia entao vou precisar de usar locks para estes contadores
						scheduled++
					}
					completeCh <- time.Since(start)
				}
			} else { // a job
				if  image == "enhance" { //mem/cpu intensive job
					cmd := exec.Command("docker", "-H", "tcp://10.5.60.2:2377", "run",  "-v", "/home/smendes:/ne/input", "-itd", "-c", cpu, "-m", memory, "-e", "affinity:makespan==", makespan, "alexjc/neural-enhance", "--zoom=2", "input/macos.jpg")
					cmd.Stdout = &out
					cmd.Stderr = &stderr

					if err := cmd.Run(); err != nil {
						fmt.Println("Not scheduled")
						notScheduled++
						fmt.Println(fmt.Sprint(err) + ": " + stderr.String())
					} else {
						//TODO: se eu usar concorrencia entao vou precisar de usar locks para estes contadores
						scheduled++
					}
					completeCh <- time.Since(start)
				}else {//cpu intensive job {
					j := strconv.FormatInt(i, 10)
					cmd := exec.Command("docker", "-H", "tcp://10.5.60.2:2377", "run", "-v", "/home/smendes:/tmp/workdir", "-w=/tmp/workdir", "-itd", "-c", cpu, "-m", memory, "-e", "affinity:makespan==",  makespan, "jrottenberg/ffmpeg", "-i", "dead.avi", "-r", "100", "-b", "700k", "-qscale", "0", "-ab", "160k", "-ar", "44100", "result"+j+".dvd", "-y")
					cmd.Stdout = &out
					cmd.Stderr = &stderr
	
					if err := cmd.Run(); err != nil {
						fmt.Println("Not scheduled")
						notScheduled++
						fmt.Println(fmt.Sprint(err) + ": " + stderr.String())
					} else {
						//TODO: se eu usar concorrencia entao vou precisar de usar locks para estes contadores
						scheduled++
					}
					completeCh <- time.Since(start)
				}
			}
		} else { // for energy algorithm
			if requestType == "service" {
                        	if image == "redis" {
	                        	cmd := exec.Command("docker", "-H", "tcp://10.5.60.2:2377", "run", "-itd", "-p", portNumber + ":" + portNumber, "-c", cpu, "-m", memory, "-e", "affinity:makespan==", makespan, "-e", "affinity:port==", portNumber, "-e", "affinity:requestclass==", requestClass,  "-e", "affinity:requesttype==", requestType, image, "--port", portNumber)
					cmd.Stdout = &out
					cmd.Stderr = &stderr
	
					if err := cmd.Run(); err != nil {
						fmt.Println("Not scheduled")
						notScheduled++
						fmt.Println(fmt.Sprint(err) + ": " + stderr.String())
					} else {
						//TODO: se eu usar concorrencia entao vou precisar de usar locks para estes contadores
						scheduled++
					}
					completeCh <- time.Since(start)
				} else {
                                	cmd := exec.Command("docker", "-H", "tcp://10.5.60.2:2377", "run", "-itd", "-p", portNumber + ":" + portNumber, "-c", cpu, "-m", memory, "-e", "affinity:requestclass==", requestClass, "-e", "affinity:makespan==", makespan, "-e", "affinity:requesttype==", requestType, "-e", "affinity:port==", portNumber, image, portNumber)
					cmd.Stdout = &out
					cmd.Stderr = &stderr
	
					if err := cmd.Run(); err != nil {
						fmt.Println("Not scheduled")
						notScheduled++
						fmt.Println(fmt.Sprint(err) + ": " + stderr.String())
					} else {
						//TODO: se eu usar concorrencia entao vou precisar de usar locks para estes contadores
						scheduled++
					}
					completeCh <- time.Since(start)
				}
			} else {
				if image == "enhance" { //mem/cpu intensive job {
					cmd := exec.Command("docker", "-H", "tcp://10.5.60.2:2377", "run", "-v", "/home/smendes:/ne/input", "-itd", "-c", cpu, "-m",  memory, "-e", "affinity:makespan==", makespan, "-e", "affinity:requestclass==", requestClass, "-e", "affinity:requesttype==", requestType, "alexjc/neural-enhance", "--zoom=2", "input/macos.jpg")
					cmd.Stdout = &out
					cmd.Stderr = &stderr
	
					if err := cmd.Run(); err != nil {
						fmt.Println("Not scheduled")
						notScheduled++
						fmt.Println(fmt.Sprint(err) + ": " + stderr.String())
					} else {
						//TODO: se eu usar concorrencia entao vou precisar de usar locks para estes contadores
						scheduled++
					}
					completeCh <- time.Since(start)
				} else {			 
					j := strconv.FormatInt(i, 10)
					cmd := exec.Command("docker", "-H", "tcp://10.5.60.2:2377", "run", "-v", "/home/smendes:/tmp/workdir", "-w=/tmp/workdir", "-itd", "-c", cpu, "-m",  memory, "-e", "affinity:requestclass==", requestClass, "-e", "affinity:makespan==", makespan, "-e", "affinity:requesttype==", requestType, "jrottenberg/ffmpeg", "-i", "dead.avi", "-r", "100", "-b", "700k", "-qscale", "0", "-ab", "160k", "-ar", "44100", "result"+j+".dvd", "-y")
					cmd.Stdout = &out
					cmd.Stderr = &stderr
	
					if err := cmd.Run(); err != nil {
						fmt.Println("Not scheduled")
						notScheduled++
						fmt.Println(fmt.Sprint(err) + ": " + stderr.String())
					} else {
						//TODO: se eu usar concorrencia entao vou precisar de usar locks para estes contadores
						scheduled++
					}
					completeCh <- time.Since(start)
				}
			}
		}		
	}
}

func session(requests int64, concurrency int64, completeCh chan time.Duration, listRequests [][]string) {
	var wg sync.WaitGroup
	n := requests / concurrency
	
	var i int64
	for i = 0; i < concurrency; i++ {
		wg.Add(1)
		go func() {
			worker(n, completeCh, listRequests)
			wg.Done()
		}()
	}
	wg.Wait()
}

func bench(requests int64, concurrency int64, listRequests [][]string) {
	start := time.Now()

	timings := make([]float64, requests)
	// Create a buffered channel so our display goroutine can't slow down the workers.
	completeCh := make(chan time.Duration, requests)
	doneCh := make(chan struct{})
	current := 0
	go func() {
		for timing := range completeCh {
			timings = append(timings, timing.Seconds())
			current++
			percent := float64(current) / float64(requests) * 100
			fmt.Printf("[%3.f%%] %d/%d containers started\n", percent, current, requests)
		}
		doneCh <- struct{}{}
	}()
	session(requests, concurrency, completeCh, listRequests)
	close(completeCh)
	<-doneCh

	total := time.Since(start)
	mean, _ := stats.Mean(timings)
	p90th, _ := stats.Percentile(timings, 90)
	p99th, _ := stats.Percentile(timings, 99)
	p50th, _ := stats.Percentile(timings, 50)

	meanMillis := mean * MILLIS_IN_SECOND
	p90thMillis := p90th * MILLIS_IN_SECOND
	p99thMillis := p99th * MILLIS_IN_SECOND
	p50thMillis := p50th * MILLIS_IN_SECOND

	fmt.Printf("\n")
	fmt.Printf("Time taken for tests: %.3fs\n", total.Seconds())
	fmt.Printf("Time per container: %.3fms [mean] | %.3fms [50th] |%.3fms [90th] | %.3fms [99th]\n", meanMillis, p50thMillis, p90thMillis, p99thMillis)
	fmt.Printf("Scheduled: %d | Not scheduled: %d\n", scheduled, notScheduled) 
}

func main() {
	var memorySent,cpuSent, memoryLimit, cpuLimit int64
	//oneHour := 1800000000000
	startTime := time.Now()
	listMakespan := make([]int64, 0)
	memoryLimit = 88583700480 // 88583700480: 88gb with 150% overbooking //59055800320; //55gb full capacity of the 5 servers
	cpuLimit = 61440 //61440 150% overbooking 40960; //40960 cpu shares total

	requests := make([][]string, 0)

	file, err := os.Open("traces.txt")	
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()
	
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		timeNow := time.Since(startTime)
		timeNow2 := timeNow.Minutes()
		fmt.Print("Current time: ")
		fmt.Println(timeNow2)		

		if timeNow2 >= 30 {
			break
		}
		fmt.Println("AQUI")
		line := scanner.Text()
		parts := strings.Split(line, ",")
                makespan := strings.Split(parts[0],":")
                cpu := strings.Split(parts[1],":")
                memory := strings.Split(parts[2],":")
                                //String[] requestClass = parts[3].split(":");
                requestClass := []string{"0","0"}
                requestType := strings.Split(parts[4],":")
		requestImage := strings.Split(parts[5],":")
		requestRate := strings.Split(parts[6],":")				
		portNumber := strings.Split(parts[7],":")	
		
		requests = append(requests, []string{makespan[1],cpu[1], memory[1], requestClass[1], requestType[1], requestImage[1], requestRate[1], portNumber[1]})
		
		memoryAux, _ := strconv.ParseInt(memory[1],10,64)
		cpuAux, _ := strconv.ParseInt(cpu[1],10,64)
		makespanAux, _ := strconv.ParseInt(makespan[1], 10, 64)
		memorySent += memoryAux
		cpuSent += cpuAux
		listMakespan = append(listMakespan, makespanAux)

		if memorySent > memoryLimit || cpuSent > cpuLimit {
			memorySent = 0
			cpuSent = 0
			var makespanAux int64		
			length := int64(len(listMakespan))

			bench(length, 1, requests)
			requests = requests[:0]
			
			for _, value  := range listMakespan {
				makespanAux += value
			}
			listMakespan = listMakespan[:0]
			waitTimeAux := int64(makespanAux / length)
			fmt.Print("Waiting: ")
			fmt.Println(waitTimeAux)
			time.Sleep(time.Second * time.Duration(waitTimeAux))
			fmt.Println("Continuing")
		}

	}
}
