# core.logic
클로저와 클로저 스크립트를 위한 논리 프로그래밍 라이브러리인 core.logic은 관계형 프로그래밍, 제약 논리 프로그래밍과 같은 Prolog 언어와 유사한 기능을 제공한다. 이 라이브러리는 William Byrd의 논문인 [Relational Programming in miniKanren: Techniques, Applications, and Implementations](http://pqdtopen.proquest.com/search.html#abstract?dispub=3380156)에 기술된 miniKanren을 그 기반으로 삼고 있으며,  [cKanren](http://www.schemeworkshop.org/2011/papers/Alvis2011.pdf)과 [αKanren](http://webyrd.net/alphamk/alphamk.pdf)까지도 확장하고 있다. 단순히 이들이 제공하는 기능을 넘어서 논리 프로그래밍을 쉽게 확장하는 것을 목표로 설계되었다.

(원문 출처: [core.logic - README.md](https://github.com/clojure/core.logic/blob/master/README.md))

## 릴리즈 및 의존성 정보

* 최신 안정 릴리즈(latest stable release): 0.8.10
	* [다른 릴리즈 버전](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.clojure%22%20AND%20a%3A%22core.logic%22)
	* [개발 스냅샷 버전](http://oss.sonatype.org/index.html#nexus-search;gav~org.clojure~core.logic~~~)

* [라이닝언(Leiningen)](http://github.com/technomancy/leiningen/) 의존성 정보:

```
[org.clojure/core.logic "0.8.10"]
```

* [메이븐(Maven)](http://maven.apache.org) 의존성 정보:

```
<dependency>
  <groupId>org.clojure</groupId>
  <artifactId>core.logic</artifactId>
  <version>0.8.10</version>
</dependency>
```

## 원자료 및 소스코드 출처
이 튜토리얼에의 소스코드와 번역 대상 문서들의 출처은 다음과 같다.

* [core.logic wiki](https://github.com/clojure/core.logic/wiki)
* [swannodette/logic-tutorial](https://github.com/swannodette/logic-tutorial)
* [frenchy64/Logic-Starter](https://github.com/frenchy64/Logic-Starter)
