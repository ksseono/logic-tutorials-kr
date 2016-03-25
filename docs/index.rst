core.logic
==============================================

클로저와 클로저 스크립트를 위한 논리 프로그래밍 라이브러리인 core.logic은 관계형 프로그래밍, 제약 논리 프로그래밍과 같은 Prolog 언어와 유사한 기능을 제공한다. 이 라이브러리는 William Byrd의 논문인 `Relational Programming in miniKanren: Techniques, Applications, and Implementations`_ 에 기술된 miniKanren을 그 기반으로 삼고 있으며, `cKanren`_ 과 `αKanren`_ 까지도 확장하고 있다. 단순히 이들이 제공하는 기능을 넘어서 논리 프로그래밍을 쉽게 확장하는 것을 목표로 설계되었다.

(원문 출처: `core.logic - README.md`_)

.. _Relational Programming in miniKanren\: Techniques, Applications, and Implementations: http://pqdtopen.proquest.com/search.html#abstract?dispub=3380156
.. _cKanren: http://www.schemeworkshop.org/2011/papers/Alvis2011.pdf
.. _αKanren: http://webyrd.net/alphamk/alphamk.pdf
.. _core.logic - README.md: https://github.com/clojure/core.logic/blob/master/README.md

릴리즈 및 의존성 정보
----------------------------------------------

* 최신 안정 릴리즈(latest stable release): 0.8.10
	* `다른 릴리즈 버전`_
	* `개발 스냅샷 버전`_     
     
* `라이닝언(Leiningen)`_ 의존성 정보: 
.. code-block:: xml

	<dependency>
		<groupId>org.clojure</groupId>
	 	<artifactId>core.logic</artifactId>
	 	<version>0.8.10</version>
	</dependency>

.. _다른 릴리즈 버전: http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.clojure%22%20AND%20a%3A%22core.logic%22
.. _개발 스냅샷 버전: http://oss.sonatype.org/index.html#nexus-search;gav~org.clojure~core.logic~~~
.. _라이닝언(Leiningen): http://github.com/technomancy/leiningen/


튜토리얼 구성 개요
----------------------------------------------

전체적으로는 다음의 세 파트로 구성되며, `core.logic wiki`_, `swannodette/logic-tutorial`_, `frenchy64/Logic-Starter`_ 의 내용을 각 파트에 맞게 재배치하고 필요에 따라 내용을 수정하여 구성한다.

.. _core.logic wiki: https://github.com/clojure/core.logic/wiki
.. _swannodette/logic-tutorial: https://github.com/swannodette/logic-tutorial
.. _frenchy64/Logic-Starter: https://github.com/frenchy64/Logic-Starter

* 파트 구성
	* :ref:`overview`
	* :ref:`quick-start`
	* :ref:`more`

.. _overview:
.. toctree::
   :maxdepth: 2
   :caption: core.logic 개요

   resources/core.logic-wiki/Features


.. _quick-start:
.. toctree::
	:maxdepth: 2
	:caption: core.logic 시작하기

	resources/logic-introduction/Home

.. _more:
.. toctree::
	:maxdepth: 2
	:caption: 기타 읽을거리

	resources/core.logic-wiki/Differences-from-The-Reasoned-Schemer