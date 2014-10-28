TARGET = onion
MKARGS = -bibtex -quiet

.PHONY: $(TARGET).pdf clean view

$(TARGET).pdf: $(TARGET).tex
	latexmk $(MKARGS) -pdf $<

clean: $(TARGET).tex
	latexmk $(MKARGS) -c $<

view: $(TARGET).pdf
	xdg-open $<
