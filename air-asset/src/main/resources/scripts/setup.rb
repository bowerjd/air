
require 'java'
require 'sass'

java_import 'com.lonelystorm.aem.air.asset.services.impl.SassCompilerImpl'

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
                content = $service.include($library, dir + "/" + name)
                return unless content

                options[:importer] = self
                options[:filename] = dir + "/" + name
                options[:syntax] = :scss
                Sass::Engine.new(content, options)
            end
        end
    end
end
