# -*- coding: utf-8 -*-
import sys
import os

extensions = []

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates']

# source_suffix = ['.rst', '.md']
source_suffix = '.rst'

# The encoding of source files.
#source_encoding = 'utf-8-sig'

# The master toctree document.
master_doc = 'index'

# General information about the project.
project = u'clojure/core.logic 튜토리얼'
copyright = u'2016, Seonho Kim, http://seonhokim.net'
author = u'Seonho Kim'

# The short X.Y version.
version = u'0.1'
# The full version, including alpha/beta/rc tags.
release = u'0.1'

language = 'ko'

exclude_patterns = ['_build']

# The name of the Pygments (syntax highlighting) style to use.
pygments_style = 'sphinx'

todo_include_todos = False


# -- Options for HTML output ----------------------------------------------

#html_theme = 'alabaster'
htmlhelp_basename = 'logic-tutorials-kr'
on_rtd = os.environ.get('READTHEDOCS', None) == 'True'

if not on_rtd:  # only import and set the theme if we're building docs locally
    import sphinx_rtd_theme
    html_theme = 'sphinx_rtd_theme'
    html_theme_path = [sphinx_rtd_theme.get_html_theme_path()]
    

# -- Options for LaTeX output ---------------------------------------------

latex_elements = {}

latex_documents = [
    (master_doc, 'logic-tutorials-kr.tex', u'logic-tutorials-kr',
     u'Seonho Kim', 'manual'),
]


# -- Options for manual page output ---------------------------------------

man_pages = [
    (master_doc, 'logic-tutorials-kr', u'logic-tutorials-kr',
     [author], 1)
]


# -- Options for Texinfo output -------------------------------------------

texinfo_documents = [
    (master_doc, 'logic-tutorials-kr', u'logic-tutorials-kr',
     author, 'logic-tutorials-kr', 'One line description of project.',
     'Miscellaneous'),
]
