package com.metacoding.chapter06

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.metacoding.chapter06.databinding.ActivityMainBinding
import com.metacoding.chapter06.databinding.DialogCountdownSettingBinding
import java.util.Timer
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var countdownSecond = 10

    //DeciSecond : 0.1초 단위의 숫자
    private var currentDeciSecond = 0
    private var currentCountdownDeciSecond = countdownSecond * 10

    //timer
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.countdownTextView.setOnClickListener {
            showCountdownSettingDialog()
        }

        binding.startButton.setOnClickListener {
            binding.startButton.isVisible = false
            binding.stopButton.isVisible = false
            binding.pauseButton.isVisible = true
            binding.lapButton.isVisible = true
            start()
        }

        binding.stopButton.setOnClickListener {
            showAlertDialog()
        }

        binding.pauseButton.setOnClickListener {
            pause()
            binding.startButton.isVisible = true
            binding.stopButton.isVisible = true
            binding.pauseButton.isVisible = false
            binding.lapButton.isVisible = false
        }

        binding.lapButton.setOnClickListener {
            lap()
        }

        initViews()

    }

    //초기화시켜주는 함수
    private fun initViews() {
        currentDeciSecond = 0
        binding.timeTextView.text = "00:00"
        binding.tickTextView.text = "0"
        binding.countdownTextView.text = String.format("%02d", countdownSecond)
        var progress = (currentCountdownDeciSecond / (countdownSecond * 10f)) * 100
        binding.countdownProgressBar.progress = progress.toInt()
    }


    private fun start() {

        timer = timer(initialDelay = 0, period = 100) {


            /**카운트 다운*/
            if (currentCountdownDeciSecond == 0) {

                //타이머를 만드는 것도 thread를 만드는 것과 동일하다.
                //데이터 업데이트
                currentDeciSecond += 1
                Log.d("currentDeciSecond", currentDeciSecond.toString())
                val minutes = currentDeciSecond.div(10) / 60
                val seconds = currentDeciSecond.div(10) % 60
                val deciSeconds = currentDeciSecond % 10

                //UI 업데이트
                runOnUiThread {
                    binding.countdownGroup.isVisible = false

                    binding.timeTextView.text =
                        String.format("%02d:%02d", minutes, seconds)
                    binding.tickTextView.text = deciSeconds.toString()
                }

            } else {
                //시간 데이터 업데이트
                currentCountdownDeciSecond--
                val seconds = currentCountdownDeciSecond.div(10)

                //progress bar 업데이트 0 ~ 100까지의 값으로 나타낸다
                var progress = (currentCountdownDeciSecond / (countdownSecond * 10f)) * 100
                binding.countdownProgressBar.progress = progress.toInt()

                binding.root.post {
                    binding.countdownTextView.text =
                        String.format("%02d", seconds)
                }
            }
        }
    }

    private fun pause() {
        timer?.cancel()
        //timer 없애기
        timer = null
    }

    private fun stop() {
        //visibility 업데이트
        binding.startButton.isVisible = true
        binding.stopButton.isVisible = true
        binding.pauseButton.isVisible = false
        binding.lapButton.isVisible = false

        //시간 초기화
        binding.countdownGroup.isVisible = true
        initViews()

        //lap 기록 지워주기
        binding.labContainerLinearLayout.removeAllViews()
    }

    private fun lap() {
        //start 전이면 그냥 종료
        if (currentDeciSecond == 0) return

        //linearLayout 받아오기
        val container = binding.labContainerLinearLayout

        //코드에서 textView 작성하기
        val lapTextView = TextView(this).apply {
            textSize = 20f  //textSize는 float으로 넣어야 한다.
            gravity = Gravity.CENTER

            val minutes = currentDeciSecond / 10 / 60
            val seconds = currentDeciSecond / 10 % 60
            val deciSeconds = currentDeciSecond % 10

            text = container.childCount.inc().toString() + String.format(
                ". %02d:%02d %01d",
                minutes,
                seconds,
                deciSeconds
            )
            setPadding(30)

        }
        container.addView(lapTextView, 0)
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("종료하시겠습니까?")
            setPositiveButton("네") { _, _ ->
                stop()
            }
            setNegativeButton("아니요", null)
        }.show()
    }

    private fun showCountdownSettingDialog() {
        AlertDialog.Builder(this).apply {

            val dialogBinding = DialogCountdownSettingBinding.inflate(layoutInflater)
            //numberPicker의 값 설정하기
            with(dialogBinding.countdownSecondPicker) {
                maxValue = 60
                minValue = 0
                value = countdownSecond
            }
            setTitle("카운트다운 설정")
            //numberPicker 창 띄우기기
            setView(dialogBinding.root)
            setPositiveButton("확인") { _, _ ->
                //데이터 업데이트
                countdownSecond = dialogBinding.countdownSecondPicker.value
                currentCountdownDeciSecond = countdownSecond * 10

                //UI 업데이트 ** 잊지말기! **
                binding.countdownTextView.text = String.format("%02d", countdownSecond)

            }
            setNegativeButton("취소", null)
        }.show()
    }
}