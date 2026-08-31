package com.animalbattle.game.domain.model

object QuestionData {

    fun getRandomQuestion(excludeIds: List<String> = emptyList()): Question {
        val available = getQuestions().filter { it.id !in excludeIds }
        return available.random()
    }

    fun getQuestions(): List<Question> = listOf(
        Question(
            id = "q1",
            questionText = "معنی کلمه «شیر» چیست؟",
            options = listOf("حیوان درنده", "نوشیدنی", "میوه", "وسیله نقلیه"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q2",
            questionText = "کدام یک حیوان گوشتخوار است؟",
            options = listOf("گوسفند", "ببر", "خرگوش", "گربه‌سان"),
            correctOptionIndex = 1
        ),
        Question(
            id = "q3",
            questionText = "بزرگترین حیوان خشکی کدام است؟",
            options = listOf("فیل", "زرافه", "کرگدن", "نهنگ"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q4",
            questionText = "معنی کلمه «عقاب» چیست؟",
            options = listOf("پرنده شکاری", "ماهی", "حیوان خانگی", "خزندگان"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q5",
            questionText = "کدام حیوان سریعترین است؟",
            options = listOf("شیر", "یوزپلنگ", "گرگ", "خرس"),
            correctOptionIndex = 1
        ),
        Question(
            id = "q6",
            questionText = "معنی کلمه «گرگ» چیست؟",
            options = listOf("حیوان گله", "حیوان درنده", "پرنده", "ماهی"),
            correctOptionIndex = 1
        ),
        Question(
            id = "q7",
            questionText = "کدام حیوان بیشترین عمر را دارد؟",
            options = listOf("سگ", "فیل", "لاک‌پشت", "شیر"),
            correctOptionIndex = 2
        ),
        Question(
            id = "q8",
            questionText = "معنی کلمه «تمساح» چیست؟",
            options = listOf("خزندگان بزرگ", "پرندگان", "ماهی‌ها", "حشرات"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q9",
            questionText = "کدام حیوان در آب زندگی می‌کند؟",
            options = listOf("شیر", "کرگدن", "تمساح", "گرگ"),
            correctOptionIndex = 2
        ),
        Question(
            id = "q10",
            questionText = "معنی کلمه «ببر» چیست؟",
            options = listOf("گربه بزرگ", "سگ بزرگ", "خرس بزرگ", "پرنده بزرگ"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q11",
            questionText = "کدام حیوان پشم تولید می‌کند؟",
            options = listOf("گوسفند", "شیر", "ببر", "عقاب"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q12",
            questionText = "معنی کلمه «کبری» چیست؟",
            options = listOf("مار زهردار", "پرنده", "حیوان خانگی", "ماهی"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q13",
            questionText = "کدام حیوان دارای خرطوم است؟",
            options = listOf("فیل", "شیر", "ببر", "گرگ"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q14",
            questionText = "معنی کلمه «پلنگ» چیست؟",
            options = listOf("گربه خالدار", "سگ شکاری", "خرس", "عقاب"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q15",
            questionText = "کدام حیوان قویترین است؟",
            options = listOf("فیل", "شیر", "ببر", "گرگ"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q16",
            questionText = "معنی کلمه «خرس» چیست؟",
            options = listOf("حیوان گیاه‌خوار و گوشت‌خوار", "حیوان دریایی", "پرنده", "خزندگان"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q17",
            questionText = "کدام حیوان می‌تواند پرواز کند؟",
            options = listOf("عقاب", "شیر", "ببر", "کرگدن"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q18",
            questionText = "معنی کلمه «گوریل» چیست؟",
            options = listOf("میمون بزرگ", "حیوان خانگی", "پرنده", "ماهی"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q19",
            questionText = "کدام حیوان با سرعت ۱۲۰ کیلومتر در ساعت می‌دود؟",
            options = listOf("یوزپلنگ", "شیر", "ببر", "خرس"),
            correctOptionIndex = 0
        ),
        Question(
            id = "q20",
            questionText = "معنی کلمه «کرگدن» چیست؟",
            options = listOf("حیوان شاخ‌دار بزرگ", "پرنده", "خزندگان", "ماهی"),
            correctOptionIndex = 0
        )
    )
}
