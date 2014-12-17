TARGET = onion
MKARGS = -bibtex

.PHONY: $(TARGET).pdf clean view

$(TARGET).pdf: $(TARGET).tex
	latexmk $(MKARGS) -pdf $<

clean: $(TARGET).tex
	latexmk $(MKARGS) -c $<

view: $(TARGET).pdf
	xdg-open $<
