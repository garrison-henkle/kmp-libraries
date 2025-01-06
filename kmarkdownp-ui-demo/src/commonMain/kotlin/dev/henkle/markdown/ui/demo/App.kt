package dev.henkle.markdown.ui.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dev.henkle.markdown.parser.treesitter.TreesitterParser
import dev.henkle.markdown.ui.KMarkdownPUI
import dev.henkle.markdown.ui.Markdown
import dev.henkle.markdown.ui.MarkdownUIComponents
import dev.henkle.markdown.ui.components.shared.MarkdownLink
import dev.henkle.markdown.ui.model.InlineUIElement
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle
import dev.henkle.markdown.ui.utils.ext.getText

private val BULLETS_TESTING = """
    2. **Quadratic Formula**:
   - The most general method to find the roots of a quadratic equation is using the quadratic formula:
   \[
   x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}
   \]
   - **Discriminant** (\(b^2 - 4ac\)): Determines the nature of the roots:
     - If positive, there are two real and distinct roots.
     - If zero, there is exactly one real root (repeated).
     - If negative, there are two complex roots.
""".trim()
    .replace(oldValue = "\\[", newValue = "$$")
    .replace(oldValue = "\\]", newValue = "$$")
    .replace(oldValue = "\\(", newValue = "$")
    .replace(oldValue = "\\)", newValue = "$")
    .replace(regex = """\[(\d+)]""".toRegex()) { match -> match.groupValues[1] }

private val EX_WITH_NESTED_BULLETED_LIST = """
#### Understanding Quadratic Equations

A **quadratic equation** is a type of polynomial equation of the form:

\[ ax^2 + bx + c = 0 \]

where:
- \( a \), \( b \), and \( c \) are constants with \( a \neq 0 \),
- \( x \) represents the unknown variable.

#### Key Features

- **Degree**: The degree of the quadratic equation is 2 because the highest power of \( x \) is 2.
- **Solutions**: The solutions to the quadratic equation are known as the roots, which can be found using various methods.

#### Methods to Solve Quadratic Equations

1. **Factoring**:
   - If the quadratic can be factored into two binomials, set each to zero to solve for \( x \).

2. **Quadratic Formula**:
   - The most general method to find the roots of a quadratic equation is using the quadratic formula:
   \[
   x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}
   \]
   - **Discriminant** (\(b^2 - 4ac\)): Determines the nature of the roots:
     - If positive, there are two real and distinct roots.
     - If zero, there is exactly one real root (repeated).
     - If negative, there are two complex roots.

3. **Completing the Square**:
   - Transform the equation into a perfect square trinomial, then solve for \( x \).

4. **Graphical Method**:
   - Plotting the quadratic equation as a parabola on the coordinate plane can visually indicate the roots where the curve intersects the x-axis.

#### Example

Let's solve \( 2x^2 + 4x - 6 = 0 \) using the quadratic formula:

Given:
\( a = 2 \), \( b = 4 \), \( c = -6 \).

\[
x = \frac{-4 \pm \sqrt{4^2 - 4 \cdot 2 \cdot (-6)}}{2 \cdot 2}
\]
\[
x = \frac{-4 \pm \sqrt{16 + 48}}{4}
\]
\[
x = \frac{-4 \pm \sqrt{64}}{4}
\]
\[
x = \frac{-4 \pm 8}{4}
\]

So the solutions are:
\[
x = 1 \quad \text{and} \quad x = -3
\]
""".trim()
    .replace(oldValue = "\\[", newValue = "$$")
    .replace(oldValue = "\\]", newValue = "$$")
    .replace(oldValue = "\\(", newValue = "$")
    .replace(oldValue = "\\)", newValue = "$")
    .replace(regex = """\[(\d+)]""".toRegex()) { match -> match.groupValues[1] }

private val EX_WITH_NESTED_NUMBERED_LIST = """
- **Quadratic Formula**:
   1. The most general method to find the roots of a quadratic equation is using the quadratic formula:
   \[
   x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}
   \]
   2. **Discriminant** (\(b^2 - 4ac\)): Determines the nature of the roots:
     1. If positive, there are two real and distinct roots.
     2. If zero, there is exactly one real root (repeated).
     3. If negative, there are two complex roots.
""".trim()
    .replace(oldValue = "\\[", newValue = " $$ ")
    .replace(oldValue = "\\]", newValue = " $$ ")
    .replace(oldValue = "\\(", newValue = " $ ")
    .replace(oldValue = "\\)", newValue = " $ ")
    .replace(regex = """\[(\d+)]""".toRegex()) { match -> match.groupValues[1] }

private val EX_MARKDOWN = """
# **Research Report: Carthage, Missouri**

## Introduction
**Carthage, Missouri, is a city *rich*** in history and culture, located in Jasper County. This report delves into various aspects of Carthage, including its historical significance, demographic details, economic landscape, and *notable attractions*.

## Historical Background
Carthage was platted in 1842 when it was chosen as the county seat for Jasper County, which was formed in 1841 [[1]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=Carthage%20was%20chosen%20as%20the%20county%20seat%2C%20the%20area%20cleared%20and%20the%20town%20platted%20in%201842)[[2]](https://digitalcommons.pittstate.edu/fa/272/#:~:text=Carthage%2C%20Missouri%20was%20platted%20in%201842%20when%20it%20was%20chosen%20as%20the%20county%20seat%20for%20Jasper%20County%2C%20formed%20in%201841). The city has a storied past, particularly during the Civil War. The Battle of Carthage, fought on July 5, 1861, was the earliest full-scale battle of the Civil War, preceding the Battle of Bull Run by 11 days [[3]](https://mostateparks.com/park/battle-carthage-state-historic-site#:~:text=Battle%20of%20Carthage%20was%20the%20earliest%20full%2Dscale%20battle%20of%20the%20Civil%20War%2C%20preceding%20Bull%20Run%20by%2011%20days). The town experienced significant turmoil during the war, including skirmishes and attacks, and was largely burned by pro-Confederate guerrillas in September 1864 [[4]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=The%20town%20experienced%20minor%20skirmishes%20and%20attacks%20throughout%20the%20war%3B%20pro%2DConfederate%20guerrillas%20burned%20most%20of%20the%20city%20%28including%20the%20courthouse%29%20in%20September%201864).

![Carthage, Missouri](https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Carthage_Missouri_10-18-2008.jpg/500px-Carthage_Missouri_10-18-2008.jpg)

## Demographics
As of the 2020 census, Carthage had a population of 15,522 [[5]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=The%20population%20was%2015%2C522%20as%20of%20the%202020%20census). The racial makeup of the city is diverse, with 62.83% White, 1.35% Black or African-American, 1.84% Native American, 1.23% Asian, 0.51% Pacific Islander, 24.14% from other races, and 8.1% from two or more races [[6]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=The%20racial%20makeup%20was%2062.83%25%20%289%2C753%29%20white%2C%201.35%25%20%28210%29%20black%20or%20African%2DAmerican%2C%201.84%25%20%28285%29%20Native%20American%2C%201.23%25%20%28191%29%20Asian%2C%200.51%25%20%2879%29%20Pacific%20Islander%2C%2024.14%25%20%283%2C747%29%20from%20other%20races%2C%20and%208.1%25%20%281%2C257%29%20from%20two%20or%20more%20races).

## Economic Landscape
Carthage has a robust economic environment with several major employers. Leggett & Platt, a Fortune 500 company manufacturing household durables, is headquartered in the town [[7]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=Leggett%20%26%20Platt%2C%20now%20a%20Fortune%20500%20company%20still%20based%20in%20Carthage%2C%20was%20founded%20in%201883). Other significant employers include H.E. Williams, Inc., Otts Foods, Schreiber Foods, and Goodman Manufacturing [[8]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=%28a%20manufacturer%20of%20commercial%20and%20industrial%20lighting%20fixtures%29%2C%20Otts%20Foods%2C%20Schreiber%20Foods%2C%20and%20Goodman%20Manufacturing%20%28all%20producing%20various%20food%20products%29%20and%20the%20Carthage%20Underground%2C%20formerly%20a%20quarry%2C%20which%20now%20serves%20as%20a%20storage%20area%20with%20climate%20control%20for%20various%20products). The Carthage Underground, a former quarry, now serves as a climate-controlled storage area for various products [[8]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=%28a%20manufacturer%20of%20commercial%20and%20industrial%20lighting%20fixtures%29%2C%20Otts%20Foods%2C%20Schreiber%20Foods%2C%20and%20Goodman%20Manufacturing%20%28all%20producing%20various%20food%20products%29%20and%20the%20Carthage%20Underground%2C%20formerly%20a%20quarry%2C%20which%20now%20serves%20as%20a%20storage%20area%20with%20climate%20control%20for%20various%20products).

## Education
The Carthage R-IX School District operates multiple educational institutions, including five elementary schools, an intermediate center, a 6th Grade Center, Carthage Jr. High School, and Carthage High School [[9]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=Carthage%20R%2DIX%20School%20District%20operates%20five%20elementary%20schools%2C%20an%20intermediate%20center%2C%20a%206th%20Grade%20Center%2C%20Carthage%20Jr.%20High%20School%20and%20Carthage%20High%20School).

## Political Representation
Carthage is part of Missouri's 7th congressional district and has been represented in the United States Congress by Eric Burlison since 2023 [[10]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=Carthage%20is%20in%20Missouri%27s%207th%20congressional%20district%20and%20has%20been%20represented%20in%20the%20United%20States%20Congress%20by%20Eric%20Burlison%20since%202023).

## Attractions and Tourism
Carthage actively promotes tourism, emphasizing its historical significance, Victorian architecture, and its location along Route 66 [[11]](https://en.wikipedia.org/wiki/Carthage,_Missouri#:~:text=In%20the%20late%2020th%20century%2C%20the%20town%20began%20actively%20courting%20tourism%2C%20emphasizing%20its%20history%20%28the%20Battle%20of%20Carthage%2C%20Victorian%20architecture%2C%20and%20Route%2066%29%2C%20as%20well%20as%20its%20proximity%20to%20the%20Precious%20Moments%20hotel%20and%20store%2C%20along%20with%20the%20popular%20country%20music%20destination%20Branson). The downtown district around the courthouse is a picturesque plaza filled with small shops, antiques, and art galleries [[12]](https://www.theroadwanderer.net/66Missouri/carthage.htm#:~:text=The%20downtown%20district%20around%20the%20courthouse%20is%20a%20picturesque%20plaza%20filled%20with%20small%20shops%2C%20antiques%20and%20art%20galleries). The city is also home to one of the few surviving drive-in theaters in America [[13]](https://www.theroadwanderer.net/66Missouri/carthage.htm#:~:text=Carthage%20is%20also%20home%20to%20one%20of%20the%20few%20surviving%20drive%2Din%20theaters%20left%20in%20America).

![Carthage Route 66 Drive-in](https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Carthage_Route_66_Drive-in.jpg/500px-Carthage_Route_66_Drive-in.jpg)

### Historic Sites
- **Battle of Carthage State Historic Site**: This site contains a quiet meadow and a spring that served as an encampment for both Union and Confederate troops during the battle [[14]](https://mostateparks.com/park/battle-carthage-state-historic-site#:~:text=Battle%20of%20Carthage%20State%20Historic%20Site%20contains%20a%20quiet%20meadow%20and%20the%20spring%20that%20made%20the%20area%20an%20encampment%20for%20both%20the%20Union%20and%20Confederate%20troops%20during%20the%20battle). The area has remained largely unchanged since the battle [[15]](https://mostateparks.com/park/battle-carthage-state-historic-site#:~:text=The%20area%20is%20little%20changed%20in%20its%20appearance%20since%20the%20battle%20was%20fought%20on%20July%205%2C%201861).
- **Jasper County Courthouse**: Built in 1895 from native gray marble, this Romanesque Revival building is a significant landmark [[16]](https://www.achp.gov/preserve-america/community/carthage-missouri#:~:text=In%201895%2C%20the%20Romanesque%20Revival%20Jasper%20County%20Courthouse%20was%20built%2C%20also%20out%20of%20the%20native%20gray%20marble). The courthouse features a mural of local history and an old-fashioned elevator with a live attendant [[17]](https://www.tripadvisor.com/Attractions-g44213-Activities-Carthage_Missouri.html#:~:text=The%20courthouse%20is%20quite%20impressive%20from%20the%20outside.%20Inside%20there%20is%20a%20huge%20mural%20of%20local%20history%20painted%20by%20a%20local%20artist%20with%20a%20recorded%20narration%20to%20explain%20it%2D%2DNice%21%20There%20are%20other%20exhibits%20about%20local%20history%20as%20well.%20AND%20there%27s%20an%20old%2Dfashioned%20elevator%20with%20a%20real%20live%20attendant%20to%20take%20you%20up%20and%20down%20the%20three%20floors%20of%20the%20courthouse.%20Take%20time%20to%20enjoy%20the%20architecture%20and%20beautiful%20features%20of%20the%20building).

### Festivals and Events
- **Marian Days**: A festival and pilgrimage for Vietnamese American Roman Catholics, celebrated since 1978 on a 28-acre campus in Carthage [[18]](https://www.experiencecarthagemo.com/#:~:text=Marian%20Days%20is%20a%20festival%20and%20a%20pilgrimage%20for%20Vietnamese%20American%20Roman%20Catholics%20celebrated%20since%201978%20on%20a%2028%2Dacre%20campus%20of%20the%20Congregation%20of%20the%20Mother%20Co%2DRedemptrix%20in%20Carthage%2C%20Missouri).
- **Maple Leaf Festival**: Known as America's Maple Leaf City, Carthage hosts an annual Maple Leaf Festival, which includes a parade and various community events [[19]](https://www.experiencecarthagemo.com/#:~:text=Carthage%2C%20Missouri%2C%20known%20as%20America%27s%20Maple%20Leaf%20City)[[20]](https://carthagenewsonline.com/category/news/#:~:text=Carthage%20Chamber%20Announces%202024%20Maple%20Leaf%20Festival%C2%AE%20Parade%20Theme%3A%20Celebrating%20America%E2%80%99s..).

## Conclusion
Carthage, Missouri, is a city that beautifully blends its rich historical heritage with modern economic and cultural developments. From its significant role in the Civil War to its thriving local businesses and vibrant community events, Carthage offers a unique and enriching experience for residents and visitors alike.

![Powers Museum](https://upload.wikimedia.org/wikipedia/commons/thumb/3/35/Powers_Museum.jpg/500px-Powers_Museum.jpg)

<br/>

<h1> hello there </h1>

   public static void main(String[] args) {}

```
public static void main(String[] args) {}
```

```java
public static void main(String[] args) {}

public static void main(String[] args) {}
```

`public static void main(String[] args) {}`

*`public static void main(String[] args) {}`*

\*wow*wow*
&amp;

This is a SETEXT h1
===================

This is a SETEXT h2
-

    fun main() {
        println("Hello world!")
    }


[[1]](https://you.com) [[2]](https://you.com)[[3]](https://you.com)

[foo](https://google.com "title")
![foo](https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Carthage_Route_66_Drive-in.jpg/500px-Carthage_Route_66_Drive-in.jpg "title")

test

[3]:
    https://you.com
    wow

- hello there!
 - general kenobi!
  - you
   - *are*
  -  a ***bold***
 - *one*
- back to level 0

  - abc!
   - *def!*
    - ***g*hi**
     - jkl
    -  mno
   - pqr
  - stu

- ht
 - gk!
  - y
   - *a*
  -  a***b***

 - *o*
- btl0




- here we go again!
  - another slightly different
    - bulleted list
      - that will be
    -  ***impossible***
  - to
- parse!

#### Quadratic equation
${'$'} x = \frac{-b \pm \sqrt{b^2-4ac}}{2a} ${'$'}
is the quadratic equation

The quadratic equation is ${'$'} x = \frac{-b \pm \sqrt{b^2-4ac}}{2a} ${'$'}.
It is an equation for sure.

---

That will be $\$5.59$ or $ \sqrt{-1} $, whichever you prefer :)

$$
i=\sqrt{-1}
$$

=======

> I am being quoted right now
> Isn't this cool
> > It's a quote...within a quote

1. general
 2. konobo
  3. WHAT IS THIS, YOU CANT INDENT A NUMBER
 3. konobo
1. general

| Item              | In Stock | Price |
| ----------------: | :------: | :---- |
| Python Hat        || 23.99 |
| SQL Hat           |   True   | 23.99 |
| Codecademy Tee    |  False   | 19.99 |
| Codecademy Hoodie |  False   | 42.99 |
| ![drive in](https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Carthage_Route_66_Drive-in.jpg/500px-Carthage_Route_66_Drive-in.jpg) |  False   | 0.99 |

[100]: https://google.com





[[101]][100]
""".trim()
    .replace(regex = """\[(\d+)]""".toRegex()) { match -> match.groupValues[1] }

val EX_MARKDOWN_2 = """
#### What is a Quadratic Equation?

A **quadratic equation** is a polynomial equation of degree 2, typically expressed in the standard form:

\[ ax^2 + bx + c = 0 \]

where:
- \( x \) is the variable,
- \( a \), \( b \), and \( c \) are coefficients, with \( a \neq 0 \) to ensure it is indeed quadratic [[1]](https://www.ncl.ac.uk/webtemplate/ask-assets/external/maths-resources/economics/algebra/quadratic-equations-and-functions.html).

The term "quadratic" comes from the Latin word "quadratus," meaning square, which refers to the \( x^2 \) term in the equation [[2]](https://www.mathsisfun.com/algebra/quadratic-equation.html).

#### Solutions of Quadratic Equations

The solutions to a quadratic equation, also known as the **roots** or **zeros**, are the values of \( x \) that satisfy the equation, making the left-hand side equal to zero [[3]](https://en.wikipedia.org/wiki/Quadratic_equation). A quadratic equation can have:
- **Two distinct real solutions**,
- **One real double root** (where both solutions are the same),
- **Two complex solutions** that are conjugates of each other [[3]](https://en.wikipedia.org/wiki/Quadratic_equation).

#### Methods of Solving Quadratic Equations

There are several methods to solve quadratic equations:

1. **Factoring**: This involves expressing the quadratic in the form \((px + q)(rx + s) = 0\) and finding the values of \( x \) that make each factor equal to zero [[1]](https://www.ncl.ac.uk/webtemplate/ask-assets/external/maths-resources/economics/algebra/quadratic-equations-and-functions.html).

2. **Completing the Square**: This method involves rearranging the equation into a perfect square trinomial.

3. **Quadratic Formula**: The most general method, applicable to all quadratic equations, is given by:

   \[
   x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}
   \]

   Here, \( b^2 - 4ac \) is known as the **discriminant**, which helps determine the nature of the roots [[4]](https://byjus.com/maths/quadratics/).

4. **Graphing**: A quadratic equation can also be solved by graphing the function \( y = ax^2 + bx + c \) and finding the points where it intersects the x-axis [[5]](https://www.cuemath.com/algebra/quadratic-equations/).

#### Example

For example, consider the quadratic equation:

\[ x^2 + 5x + 6 = 0 \]

To solve it by factoring, we look for two numbers that multiply to \( 6 \) (the constant term) and add to \( 5 \) (the coefficient of \( x \)). The numbers \( 2 \) and \( 3 \) satisfy these conditions, so we can factor the equation as:

\[ (x + 2)(x + 3) = 0 \]

Setting each factor to zero gives the solutions:

\[ x + 2 = 0 \quad \Rightarrow \quad x = -2 \]
\[ x + 3 = 0 \quad \Rightarrow \quad x = -3 \]

Thus, the roots of the equation are \( x = -2 \) and \( x = -3 \) [[1]](https://www.ncl.ac.uk/webtemplate/ask-assets/external/maths-resources/economics/algebra/quadratic-equations-and-functions.html).

Feel free to ask if you need more details or examples!
""".trim()
    .replace(oldValue = "\\[", newValue = "$$")
    .replace(oldValue = "\\]", newValue = "$$")
    .replace(oldValue = "\\(", newValue = "$")
    .replace(oldValue = "\\)", newValue = "$")
    .replace(regex = """\[(\d+)]""".toRegex()) { match -> match.groupValues[1] }

val EX_MARKDOWN_3 = """
Here we go again

1. level 0
  2. level 2
    3. level 4
      4. level 6
   5. level 3
 6. level 1
7. level 0 (2)

  - wow (2)
    - bullets (4)
     - that $ x = (5) $
     - *have* (5)
    - different `WOW CODE` (4)
  - **offsets**  (2)
   - jumping around (3)

This one is a pain to parse.
""".trim()

private val EX_NON_CITATION_LINKS = """
#### Best Headphones Comparison
Based on my research, here are some of the top resources for comparing the best headphones:
1. **RTINGS Headphones Comparison Tool**: This tool allows you to compare various headphones side-by-side based on specifications, features, and user ratings. You can find it [here](https://www.rtings.com/headphones/tools/compare)[[1]](https://www.rtings.com/headphones/tools/compare).
2. **TechRadar’s Best Headphones Guide**: This guide provides a curated list of the best headphones across different categories, including budget, premium, and specific use cases like noise cancellation. You can view it [here](https://www.techradar.com/news/audio/portable-audio/best-headphones-1280340)[[2]](https://www.techradar.com/news/audio/portable-audio/best-headphones-1280340).
3. **CNET’s Best Headphones for 2024**: CNET offers a detailed list of top headphones, including their features and performance evaluations. Check it out [here](https://www.cnet.com/tech/mobile/best-headphones/)[[3]](https://www.cnet.com/tech/mobile/best-headphones/).
4. **PCMag’s Best Headphones**: This resource provides a list of the best headphones with detailed reviews and comparisons based on various criteria. You can find it [here](https://www.pcmag.com/picks/the-best-headphones)[[4]](https://www.pcmag.com/picks/the-best-headphones).
These resources should provide you with a comprehensive comparison of the best headphones currently available on the market.
""".trim()

private val BUILD_ANNOTATION_CRASH_BUG_REPRODUCER = """
This will crash: **${'$'}18,000** and **${'$'}24,000**
""".trim()

private val NUMBER_NINE_HEADER_FAILS = """
Sure! Here's a comprehensive example showcasing various Markdown features, including tables, bullet points, and LaTeX formatting.

#### **Markdown Features Example**

##### **1. Headers**
###### This is a header level 6

##### **2. Emphasis**
- *This text is italicized.*
- **This text is bold.**
- ***This text is bold and italicized.***

##### **3. Lists**
- **Unordered List:**
  - Item 1
  - Item 2
    - Subitem 2.1
    - Subitem 2.2
- **Ordered List:**
  1. First item
  2. Second item
  3. Third item

##### **4. Links**
- [This is a link to You.com](https://you.com)

##### **5. Images**
![Sample Image](https://via.placeholder.com/150)

##### **6. Blockquotes**
> This is a blockquote. It allows you to quote text or highlight information.

##### **7. Code**
- **Inline Code:** Here is `inline code`.
- **Code Block:**
  ```
  def hello_world():
      print("Hello, World!")
  ```

##### **8. Tables**
| Header 1 | Header 2 | Header 3 |
|----------|----------|----------|
| Row 1    | Data 1   | Data 2   |
| Row 2    | Data 3   | Data 4   |

##### **9. Horizontal Rule**
---

##### **10. LaTeX Formatting**
Here's an example of an equation in LaTeX:

The quadratic formula is given by:
${'$'}${'$'}
x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}
${'$'}${'$'}

---

Feel free to ask if you need more examples or explanations about any specific Markdown feature!
""".trim()

private val BULLETED_LIST_BOLD_FAILING = """
2. **Subsequent Rounds**:
   - By early 2024, Perplexity had achieved a valuation of **$2.5 billion to $3 billion** while raising at least **$250 million** [[1]](https://techcrunch.com/2024/04/23/perplexity-is-raising-250m-at-2-point-5-3b-valuation-ai-search-sources-say/).
   - The company is currently in the final stages of raising **$500 million**, which is expected to elevate its valuation to **$9 billion** [[2]](https://www.cnbc.com/2024/11/05/perplexity-ai-nears-500-million-funding-round-at-9-billion-valuation.html).
""".trim().preprocessMarkdownRegex()

private const val ESCAPED_DOLLAR_SIGN = "\\$"
private const val MATH_BLOCK_DELIMITER = "$$"
private const val INLINE_MATH_DELIMITER = "$"
/**
 * Performs the markdown pre-processing.
 *
 * The following replacements are made:
 * - OpenAI LaTeX notation (`\(`, `\)`, `\[`, and `\]`) is replaced with standard markdown math notation
 *     - The parenthesis delimiters are for inline math and should be converted to the standard markdown `$`
 *       delimiter
 *     - The bracket delimiters are for math blocks and should be converted to the standard markdown `$$` delimiter
 * - Escapes dollar signs that need to be escaped
 *     - We can ignore the following classes of dollar signs:
 *         - Already escaped dollar signs e.g. `\$`
 *         - Properly delimited markdown math blocks e.g. `$$`
 *         - Dollar signs followed by a number (which is usually a price) e.g. `$1.00` or `$ 1.00`
 */
private fun String.preprocessMarkdown(): String =
    StringBuilder(length).apply {
        val input = this@preprocessMarkdown
        var i = 0
        var c0: Char
        var c1: Char
        var c2: Char
        if (input.isNotEmpty() && input[0] == '$') {
            if (input.length >= 2 && input[1] == '$') {
                append(MATH_BLOCK_DELIMITER)
                i += 2
            } else {
                append(ESCAPED_DOLLAR_SIGN)
                i++
            }
        }
        while (i < input.length) {
            c0 = input[i]
            when {
                c0 == '$' -> {
                    if (i + 1 < input.length) {
                        c1 = input[i + 1]
                        when {
                            c1 == '$' -> {
                                append(MATH_BLOCK_DELIMITER)
                                i += 2
                                continue
                            }
                            c1.isDigit() -> {
                                append(ESCAPED_DOLLAR_SIGN)
                                append(c1)
                                i += 2
                                continue
                            }
                        }
                    }
                    if (i + 2 < input.length) {
                        c1 = input[i + 1]
                        c2 = input[i + 2]
                        if (c1.isWhitespace() && c2.isDigit()) {
                            append(ESCAPED_DOLLAR_SIGN)
                            append(c1)
                            append(c2)
                            i += 3
                            continue
                        }
                    }
                }
                c0 == '\\' && i + 1 < input.length -> {
                    c1 = input[i + 1]
                    when (c1) {
                        '(', ')' -> {
                            append(INLINE_MATH_DELIMITER)
                            i += 2
                            continue
                        }
                        '[', ']' -> {
                            append(MATH_BLOCK_DELIMITER)
                            i += 2
                            continue
                        }
                        '$' -> {
                            append(ESCAPED_DOLLAR_SIGN)
                            i += 2
                            continue
                        }
                    }
                }
            }
            append(c0)
            i++
        }

    }.toString()

private val nonMathDollarSignRegex = "(?<!\$|\\\\)\\$(?=\\s?\\d)".toRegex()
private fun String.preprocessMarkdownRegex(): String =
    StringBuilder(this)
        .apply {
            nonMathDollarSignRegex
                .findAll(this@preprocessMarkdownRegex)
                .toList()
                .asReversed()
                .forEach { match ->
                    deleteRange(match.range.first, match.range.last + 1)
                    insert(match.range.first, "\\$")
                }
        }.toString()
        .replace(oldValue = "\\[", newValue = "$$")
        .replace(oldValue = "\\]", newValue = "$$")
        .replace(oldValue = "\\(", newValue = "$")
        .replace(oldValue = "\\)", newValue = "$")

private val BUG = """
    Altana has raised a total of **$322 million** in funding as of its most recent Series C round in July 2024.
    This includes a **$200 million Series C round** led by Thomas Tull’s US Innovative Technology Fund, which brought the
    company to a valuation of $1 billion [[1]](https://altana.ai/resources/series-c-valuation)[[2]](https://news.crunchbase.com/venture/supply-chain-startup-unicorn-altana/)[[3]](https://www.forbes.com/sites/richardnieva/2024/07/29/altana-unicorn-fundraise-200-million/). Prior to this,
    Altana raised **$100 million in a Series B round** in 2022.
""".trimIndent().preprocessMarkdown()


@Composable
fun App() {
    val parser = remember { KMarkdownPUI(markdownParser = TreesitterParser()) }
    Markdown(
        modifier = Modifier
            .background(color = Color.White)
            .padding(all = 5.dp),
        markdown = BUG,
        parser = parser,
        linkHandler = { label, url ->
            Logger.e("KMarkdownP Demo") { "Clicked on url labeled '$label': '$url'" }
        },
        spacing = 10.dp,
        components = MarkdownUIComponents(
            inlineMath = { element -> LatexView(text = element.equation) },
            mathBlock = { element -> LatexView(text = element.equation) },
            inlineLink = { element ->
                val (processedLabel, isCitation) = element.label.firstOrNull()?.let { labelElement ->
                    // We need to strip the enclosing brackets that the BE sends. These are always in Text elements
                    if (labelElement is UIElement.Text) {
                        val match = bracketRegex.find(input = labelElement.text)
                        val citationNumber = match?.groupValues?.getOrNull(index = 1)
                        if (citationNumber != null) {
                            labelElement.copy(text = AnnotatedString(text = citationNumber)) to true
                        } else {
                            labelElement to false
                        }
                    } else {
                        null
                    }
                } ?: (UIElement.Text(text = AnnotatedString(text = "")) to false)

                if (isCitation) {
                    MarkdownLink(
                        title = element.title,
                        label = listOf(processedLabel),
                        labelRaw = element.labelRaw,
                        style = LocalMarkdownStyle.current.inlineLink.linkStyle,
                    )
                } else {
                    val markdownStyle = LocalMarkdownStyle.current
                    Text(
                        text = AnnotatedString(
                            text = element.label.getText(),
                            paragraphStyle = ParagraphStyle(lineHeight = markdownStyle.text.lineHeight),
                            spanStyle = SpanStyle(
                                color = Color.Blue,
                                fontSize = markdownStyle.text.fontSize,
                                fontWeight = markdownStyle.text.fontWeight,
                                fontFamily = markdownStyle.text.fontFamily,
                                letterSpacing = markdownStyle.text.letterSpacing,
                            ),
                        ),
                    )
                }
            }
        ),
        getInlineContentAlignment = { element ->
            when (element) {
                is InlineUIElement.Link -> {
                    val isCitation = element.label
                        .firstOrNull()
                        ?.let { it as? UIElement.Text }
                        ?.text
                        ?.contains(regex = bracketRegex)
                        ?: false
                    if (isCitation) PlaceholderVerticalAlign.Center else PlaceholderVerticalAlign.TextBottom
                }

                is InlineUIElement.Code,
                is InlineUIElement.Math -> null
            }
        },
    )
}

private val bracketRegex = """\[(.+)]""".toRegex()
