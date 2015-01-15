
require 'java'
require 'sass'

java_import 'com.lonelystorm.air.asset.services.impl.SassCompilerImpl'

# https://github.com/sass/sass/blob/stable/lib/sass/importers/filesystem.rb
module Sass
    module Importers
        class ImportAemRepository < Base
            attr_accessor :root

            # Creates a new filesystem importer that imports files relative to a given path.
            #
            # @param root [String] The root path.
            #   This importer will import files relative to this path.
            def initialize(root)
                @root = root
            end

            # @see Base#find_relative
            def find_relative(name, base, options)
                _find(File.dirname(base), name, options)
            end

            # @see Base#find
            def find(name, options)
                _find(@root, name, options)
            end

            # @see Base#to_s
            def to_s
                @root
            end

            # @see Base#key
            def key(name, options)
                [self.class.name + ":" + File.dirname(File.expand_path(name)), File.basename(name)]
            end

            def _find(dir, name, options)
                content = $service.include(dir + "/" + name)
                return unless content

                options[:importer] = self
                options[:filename] = dir + "/" + name
                options[:syntax] = :scss
                Sass::Engine.new(content, options)
            end
        end
    end
end

$cacheStore = Sass::CacheStores::Memory.new

def compile(content, filename, loadPaths)
    engine = Sass::Engine.new(content, {
        :style => :compressed,
        :cache_store => $cacheStore,
        :filename => filename,
        :load_paths => loadPaths,
        :syntax => :scss,
        :filesystem_importer => Sass::Importers::ImportAemRepository
    })

    return engine.render()
end
