
engine = Sass::Engine.new($content, {
    :style => :compressed,
    :cache => false,
    :filename => $filename,
    :load_paths => $loadPaths,
    :syntax => :scss,
    :filesystem_importer => Sass::Importers::ImportAemRepository
})

# result, map = engine.render_with_sourcemap('somefile.css')
result = engine.render()
